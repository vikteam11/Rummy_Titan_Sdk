package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.FragmentWithdrawRummyBinding
import com.rummytitans.sdk.cardgame.models.WithdrawalMethodModel
import com.rummytitans.sdk.cardgame.ui.WebViewActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.WithdrawViewModel
import com.rummytitans.sdk.cardgame.ui.wallet.withdrawal.WithdrawalDoneActivity
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.sdk.cardgame.utils.showBottomSheetWebView
import com.rummytitans.sdk.cardgame.utils.showToolTip
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.*
import android.text.InputType
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.florent37.viewtooltip.ViewTooltip
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.wallet.WithdrawalTdsAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.WithdrawalMethodsAdapter
import com.rummytitans.sdk.cardgame.widget.inputFilter.InputRegexFilter
import com.rummytitans.sdk.cardgame.widget.inputFilter.MaxAmountRegexFilter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FragmentWithdraw : BaseFragment(), MainNavigationFragment,
    BaseNavigator, WithdrawalNavigator,WithDrawTdsListener,
    WithdrawalMethodSelectionListener {


    var alertWithdrawalDialog: Dialog? = null
    lateinit var binding: FragmentWithdrawRummyBinding
    lateinit var viewModel: WithdrawViewModel
    private val REQUEST_WITHDRAWAL_DONE = 11

    companion object {
        fun newInstance(currentBalance: Double, widText: String, totalWinning: Double)
                : FragmentWithdraw {
            val frag = FragmentWithdraw()
            val bundle = Bundle()
            bundle.putDouble("currentBalance", currentBalance)
            bundle.putString("widText", widText)
            bundle.putDouble("totalWinnig", totalWinning)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onWdMethodSelect(withdrawalMethod: WithdrawalMethodModel) {
        viewModel.withdrawalMethod.set(withdrawalMethod)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setTheme(inflater)
        setLanguage()
        viewModel = ViewModelProvider(this)
            .get(WithdrawViewModel::class.java)
        binding =
            FragmentWithdrawRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
                lifecycleOwner = this@FragmentWithdraw
                viewmodel = this@FragmentWithdraw.viewModel
            }
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigator = this
        viewModel.navigatorAct = this
        viewModel.myDialog = MyDialog(requireActivity())
        viewModel.getWithdrawalOption()
        binding.icBack.setOnClickListener { goBack() }

        viewModel.currentBalance.value = arguments?.getDouble("currentBalance")
        viewModel.withdrawtext.value = arguments?.getString("widText")
        viewModel.winnings = arguments?.getDouble("totalWinnig") ?: 0.0

        viewModel.isWithdrawalEnable.set((viewModel.currentBalance.value?:0.0)>1.0)

        binding.edtWithdrawAmount.setOnTouchListener { _, _ ->
            if (!viewModel.isWithdrawalEnable.get())
                showWarningMessageView("Amount should be greater than min withdrawal amount.")
            return@setOnTouchListener !viewModel.isWithdrawalEnable.get()
        }

        viewModel.withdrawalModel.observe(viewLifecycleOwner) {
            val filter=
                InputRegexFilter(
                    "^[1-9][0-9]*"
                )
            val maxLimit= it.WithdrawalOptions.map { it.Limit }.maxByOrNull {limit-> limit }!!
            val textLength= if(viewModel.currentBalance.value?:100.0 > maxLimit)
                maxLimit.toInt()
            else
                viewModel.currentBalance.value?.toInt()?:100
            binding.edtWithdrawAmount.filters = arrayOf(filter,
                MaxAmountRegexFilter(
                    textLength
                ) {
                    kotlin.runCatching {
                        val maxAmount =
                            it.WithdrawalOptions.map { it.Limit }.maxByOrNull { limit -> limit }!!
                        val msg = if (viewModel.currentBalance.value ?: 100.0 > maxAmount)
                            getString(R.string.withdrawal_limit_exceed)
                        else
                            getString(R.string.withdrawal_ammount_should_not_exceed)
                        showWarningMessageView(msg)
                    }
                })
            binding.rvWithdrawalMethods.apply {
                if (adapter == null) {
                    layoutManager = LinearLayoutManager(context)
                    adapter =
                        WithdrawalMethodsAdapter(
                            it.WithdrawalOptions.filter { !it.Disable },
                            this@FragmentWithdraw,
                            viewModel.selectedColor
                        )
                } else {
                    (adapter as? WithdrawalMethodsAdapter)?.apply {
                        listResponse = it.WithdrawalOptions.filter { !it.Disable }
                        notifyDataSetChanged()
                    }
                }
            }
        }
        initClicks()
        observeTdsOnAmount()
        initListener()

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
            )
        )
    }

    private fun initClicks() {
        binding.tncHeader.setOnClickListenerDebounce {
            activity?.showBottomSheetWebView(
                url = viewModel.withdrawalModel.value?.TdsUrl?:"",
                color = viewModel.selectedColor.get() ?: "",
                getString(R.string.tds_calculation)
            )
        }
        binding.ivSupport.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.Helpdesk,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
                )
            )
            startActivity(
                Intent(activity, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )
        }
        binding.includeBankDetail.bankExpand.setOnClickListener {
            viewModel.toggleBankDetails()
        }

        binding.ivTransactionHistory.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.TransactionalHistory,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
                )
            )
            startActivity(
                Intent(activity, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "recent")
                    .putExtra("tab", 0)
                    .putExtra("currentBalance", viewModel.currentBalance.value ?: 0.0)
            )
        }

        binding.btnWithdrawal.setOnClickListenerDebounce(debounceTime=1000L) {
            onWithdrawClick()
        }
    }

    private fun observeTdsOnAmount(){
        viewModel.tdsList.observe(viewLifecycleOwner){tdsList->
            binding.shimmerViewContainer.stopShimmer()
            if (tdsList.isEmpty()){
                binding.cardTds.visibility=View.GONE
                return@observe
            }else{
                binding.cardTds.visibility=View.VISIBLE
                binding.recycleTds.adapter = WithdrawalTdsAdapter(tdsList,this)
            }
        }
    }

    var rxDisposable: Disposable?=null

    private fun initListener() {
        if (!viewModel.isWithdrawalEnable.get())
            binding.edtWithdrawAmount.inputType= InputType.TYPE_NULL

        binding.edtWithdrawAmount.addTextChangedListener {
            kotlin.runCatching {
                if (!TextUtils.isEmpty(it.toString())) {
                    if ((it?.toString()?.length ?: 0) > 0) {
                        it.toString().toInt().let { amount ->
                            viewModel.finalWithdrawalAmount.set("")
                            if (amount<1) {
                                viewModel.withdrawalAmount.set(0)
                                viewModel.isValidAmount.set(false)
                                selectWithdrawalMethod(0)
                                return@addTextChangedListener
                            }
                            if (amount > (viewModel.currentBalance.value ?: 0.0)) {
                                showError(getString(R.string.withdrawal_ammount_should_not_exceed))
                                viewModel.isValidAmount.set(false)
                                return@addTextChangedListener
                            }else {
                                val withdrawalList=viewModel.withdrawalModel.value?.WithdrawalOptions?.filter { !it.Disable }
                                if(amount < (withdrawalList?.elementAtOrNull(0)?.MinLimit ?: 0.0)){
                                    selectWithdrawalMethod(0)
                                    viewModel.isValidAmount.set(false)
                                    return@addTextChangedListener
                                }

                                withdrawalList?.indexOfFirst { it.minLimit<=amount && amount<=it.Limit }
                                    .also {selectedModelIndex->
                                        if (selectedModelIndex==-1) {
                                            showError(getString(R.string.withdrawal_limit_exceed))
                                            selectWithdrawalMethod(withdrawalList?.size?.minus(1))
                                            viewModel.isValidAmount.set(false)
                                            return@also
                                        }
                                        selectWithdrawalMethod(selectedModelIndex)
                                        viewModel.isValidAmount.set(true)
                                    }
                            }
                        }
                    } else {
                        viewModel.isValidAmount.set(false)
                        viewModel.finalWithdrawalAmount.set("")
                        binding.edtWithdrawAmount.setText("")
                        viewModel.withdrawalAmount.set(0)
                        selectWithdrawalMethod(0)
                    }
                } else {
                    viewModel.finalWithdrawalAmount.set("")
                    viewModel.isTdsCalculating.set(false)
                    viewModel.withdrawalAmount.set(0)
                    selectWithdrawalMethod(0)
                }
            }
        }

        rxDisposable =  RxTextView.afterTextChangeEvents(binding.edtWithdrawAmount)
            .skipInitialValue()
            .debounce(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val amountInString=it.editable().toString()
                val amount = if (!TextUtils.isEmpty(amountInString) && TextUtils.isDigitsOnly(amountInString))amountInString.toInt() else 0
                if(amount >= (viewModel.withdrawalMethod.get()?.minLimit
                        ?: 100) && viewModel.isValidAmount.get()
                ){
                    binding.shimmerViewContainer.startShimmer()
                    binding.cardTds.visibility = View.VISIBLE
                    viewModel.withdrawalAmount.set(amount)
                    viewModel.getTdsOnAmount(amount)
                }else{
                    viewModel.withdrawalAmount.set(0)
                    binding.cardTds.visibility = View.GONE
                }
            }
    }

    private fun selectWithdrawalMethod(index: Int?=0) {
        val withdrawalList=viewModel.withdrawalModel.value?.WithdrawalOptions?.filter { !it.Disable }
        withdrawalList?.firstOrNull { it.applicableMethod }?.let{
            it.applicableMethod=false
        }
        val selectedModel=withdrawalList?.elementAtOrNull(index?:0)
        selectedModel?.applicableMethod=true
        viewModel.withdrawalMethod.set(selectedModel)
        binding.rvWithdrawalMethods.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        rxDisposable?.dispose()
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        binding.edtWithdrawAmount.clearFocus()
    }

    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            activity?.window?.let {it->
                if (viewModel.showWithdrawalStatusView.get()==2)
                    it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                else {
                    WindowCompat.setDecorFitsSystemWindows(it,true)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_WITHDRAWAL_DONE) {
            val backToWallet = data?.getBooleanExtra("wallet", false) ?: false
            if (backToWallet) {
                goBack()
            }
        }
    }

    private fun onWithdrawClick() {
        viewModel.apply {
            if (TextUtils.isEmpty(finalWithdrawalAmount.get()))return@apply
            withdrawalMethod.get()?.also { withdrawalMethod ->
                when {
                    withdrawalAmount.get()?:0 < withdrawalMethod.MinLimit ?: 0.0 -> {
                        val message =
                            getString(R.string.withdraw_money_need_grater_then) + withdrawalMethod.MinLimit + " " + getString(
                                R.string.withdraw_money_need_grater_then1
                            )
                        showError(message)
                    }
                    withdrawalAmount.get()?:0 > currentBalance.value ?: 0.0 ->
                        showError(R.string.withdraw_money_should_not_exced)

                    withdrawalAmount.get()?:0 > withdrawalMethod.Limit ?: 0.0 ->
                        showError(
                            getStringResource(R.string.frag_withdraw_max_limit))
                    else -> viewModel.withdrawalMyAmount()
                }
            }
        }
    }

    private fun calculateValue(value: Double): String {
        val rrr = DecimalFormat("#####0.00").format(value)
        return if (rrr.equals("nan", ignoreCase = true)) ""
        else rrr + ""
    }


    override fun onWithdrawalResponseReceived(responseStatus: Boolean) {
        val intent = Intent(requireActivity(), WithdrawalDoneActivity::class.java)
        intent.putExtra(MyConstants.INTENT_PASS_WITHDRAWAL, viewModel.withdrawalStatusModel.get())
        startActivityForResult(intent, REQUEST_WITHDRAWAL_DONE)
        if (responseStatus) {
            activity?.finish()
        }
        requireActivity().overridePendingTransition(0, 0)
    }

    override fun onTandCClick(withdrawalMethod: WithdrawalMethodModel) {
        if (URLUtil.isValidUrl(withdrawalMethod.TnCUrl)) {
            startActivity(
                Intent(context, WebViewActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_WEB_URL, withdrawalMethod.TnCUrl)
                    .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, getString(R.string.app_name_rummy))
            )
        }
    }

    override fun onUpdateRequired(message: String) {
        if (activity is CommonFragmentActivity) {
            //(activity as? CommonFragmentActivity)?.showAppUpdateDialog(message)
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        showErrorMessageView(throwable?.message ?: "")
    }

    override fun showMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "")
    }

    override fun showError(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        if (message == 0) return
        showErrorMessageView(message)
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun showToolTipMessage(view: View, tooltip: String) {
        (context as? Activity)?.let { act ->
            showToolTip(act,view,tooltip, ViewTooltip.Position.TOP,4000L)
        }
    }
}


interface WithDrawTdsListener{
    fun showToolTipMessage(view:View,tooltip:String)
}

interface WithdrawalNavigator {
    fun onUpdateRequired(message: String)
    fun onWithdrawalResponseReceived(responseStatus: Boolean)
}

interface WithdrawalMethodSelectionListener {
    fun onWdMethodSelect(withdrawalMethod: WithdrawalMethodModel)
    fun onTandCClick(withdrawalMethod: WithdrawalMethodModel)
}