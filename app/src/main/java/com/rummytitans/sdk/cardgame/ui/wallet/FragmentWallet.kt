package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCase
import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCaseBuilder
import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCaseBuilder.Companion.Withdrawal_Wallet_Section
import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCaseListener
import com.rummytitans.sdk.cardgame.bubbleview.BubbleShowCaseSequence
import com.rummytitans.sdk.cardgame.databinding.DialogTeamProfileformRummyBinding
import com.rummytitans.sdk.cardgame.databinding.DialogWalletRedeemCodeRummyBinding
import com.rummytitans.sdk.cardgame.databinding.FragmentWalletRummyBinding
import com.rummytitans.sdk.cardgame.models.HeaderItemModel
import com.rummytitans.sdk.cardgame.models.WalletInfoModel
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.games.tickets.GamesTicketActivity
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.wallet.adapter.WalletBonusAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.model.WalletRedeemCodeModel
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.WalletViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.MyConstants.REQUEST_UPDATE_VERIFY_DETAILS
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.widget.ScratchView
import kotlinx.android.synthetic.main.fragment_wallet_rummy.*
import java.util.*

class FragmentWallet : BaseFragment(),
    BaseNavigator, OnOfferBannerClick, HideKeyboardInterface,
    BubbleShowCaseSequence.OnSkipInterface, WalletNavigator {


    lateinit var viewModel: WalletViewModel
    lateinit var binding: FragmentWalletRummyBinding
    private var mRedeemCouponDialog: Dialog? = null
    var isBubbleShowcaseVisible = false
    private var mShowCaseBuilder:BubbleShowCaseBuilder?= null

    companion object {
        const val REQUEST_CODE_UPDATE_WALLET = 1011
        fun newInstance(isActivity: Boolean, reddemCode: String = ""): FragmentWallet {
            val frag = FragmentWallet()
            val bundle = Bundle()
            bundle.putBoolean("isActivity", isActivity)
            bundle.putString(MyConstants.INTENT_PASS_OPEN_REDDEM, reddemCode)
            frag.arguments = bundle
            return frag
        }
    }

    override fun requestData(){
        viewModel.isLoading.set(true)
        viewModel.fetchWalletData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        requireActivity().apply {
            if(resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE)
                changeActivityOrientation(Configuration.ORIENTATION_PORTRAIT)
        }
        setTheme(inflater)
        setLanguage()
        viewModel = ViewModelProvider(
            this
        ).get(WalletViewModel::class.java)
        binding = FragmentWalletRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
            lifecycleOwner = this@FragmentWallet
            viewmodel = this@FragmentWallet.viewModel
            viewModel.navigator = this@FragmentWallet
            viewModel.navigatorAct = this@FragmentWallet
            viewModel.myDialog = MyDialog(requireActivity())
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getBoolean("isActivity")?.let { viewModel.isAvtivity.set(it) }
       // viewModel.isLoading.set(true)
        //viewModel.fetchWalletData()
        viewModel.data.observe(viewLifecycleOwner) {
            showShowcase()
            if (viewModel.prefs.isWalletShowCaseNew) {
                arguments?.getString(MyConstants.INTENT_PASS_OPEN_REDDEM)?.let { code ->
                    if (code != "") {
                        viewModel.isBottomSheetVisible.set(true)
                        viewModel.availableCode.set(code)
                    }
                }
            }
            setGraph(it)

            binding.rvBones.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = WalletBonusAdapter(it.Balance.BonusList,this@FragmentWallet) { onClickBonusItem() }
            }
        }

        binding.btnTickets.setOnClickListener {
            if (!ClickEvent.check(ClickEvent.BUTTON_CLICK) || viewModel.isLoading.get()) return@setOnClickListener
            startActivityForResult(Intent(activity, GamesTicketActivity::class.java),123)
            viewModel.analyticsHelper.apply {
                addTrigger(AnalyticsKey.Screens.Wallet,AnalyticsKey.Screens.RummyTickets)
                fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.GameTickets,
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                    )
                )
            }
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.fetchWalletData()
        }

        btnMyRecentTransactions.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ViewStatement,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                )
            )
            startActivity(
                Intent(activity, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "recent")
                    .putExtra("tab", 0)
                    .putExtra(
                        "currentBalance", viewModel.walletInfo.value?.Balance?.TotalAmount ?: 0.0
                    )
            )
        }

        btnScratchCard.setOnClickListener {

        }

        binding.btnRedeem.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ReedemCoupon,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                )
            )
            showRedeemCouponCodeDialog()
        }

        btnWithdrawMoney.setOnClickListener {
            if(!viewModel.isAddressVerified){
                launchAddressVerificationScreen("")
            }else if (!viewModel.isVerified.get()) {
                //showError(getString(R.string.fragm_wallet_withdrawal_verify_message))
                startActivity(Intent(requireContext(), CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "verify"))
            } else {
                viewModel.analyticsHelper.apply {
                    addTrigger(AnalyticsKey.Screens.Wallet,AnalyticsKey.Screens.WithdrawMoney)
                    fireEvent(
                        AnalyticsKey.Names.ButtonClick, bundleOf(
                            AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.WithdrawMoney,
                            AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                        )
                    )
                }
                activity?.startActivityForResult(
                    Intent(activity, CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "withdraw")
                        .putExtra(
                            "currentBalance", viewModel.walletInfo.value?.Balance?.Winning ?: 0.0
                        )
                        .putExtra("widText", viewModel.walletInfo.value?.UserInfo?.WidText ?: "")
                        .putExtra(
                            "totalWinnig", viewModel.walletInfo.value?.Balance?.Winning ?: 0.0
                        ),REQUEST_CODE_UPDATE_WALLET
                )
            }
        }

        binding.scratchView.setRevealListener(object :ScratchView.IRevealListener {
            override fun onRevealed(iv:ScratchView) {
                binding.scratchView.visibility = View.GONE
                viewModel.updateScratchCard()
            }

            override fun onRevealPercentChangedListener(siv: ScratchView, percent: Float) {
                if (percent > 0.20) {
                    onRevealed(binding.scratchView)
                }
            }
        })
        binding.imgCloseScratchCard.setOnClickListener {
            binding.scratchView.init()
            viewModel.closeScratch()
        }

        hideKeyboard()
        viewModel.analyticsHelper.apply {
            fireEvent(
                AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            if (::viewModel.isInitialized)
                viewModel.fetchWalletData()
        }
    }

    private fun onClickBonusItem() {

    }

    private fun showRedeemCouponCodeDialog() {
        val dialogView: DialogWalletRedeemCodeRummyBinding? = DataBindingUtil.inflate(
            layoutInflater, R.layout.dialog_wallet_redeem_code_rummy,
            null, false
        )
        
        val model = WalletRedeemCodeModel(viewModel.prefs)

        activity?.let { activity ->
            dialogView?.let {
                dialogView.item = model
                dialogView.viewModel = this@FragmentWallet.viewModel
                mRedeemCouponDialog = MyDialog(activity).getFullScreenDialog(dialogView.root)
                mRedeemCouponDialog?.show()

                dialogView.editText.addTextChangedListener {
                    if (it.toString().isNotEmpty())
                        dialogView.inputLayout1.error = null
                    model.validForRefercode.set(it.toString().isNotEmpty())

                }
                dialogView.txtCancel.setOnClickListener {
                    mRedeemCouponDialog?.dismiss()
                }
                dialogView.viewYesOverlay.setOnClickListener {
                    dialogView.txtYes.performClick()
                }
                dialogView.txtYes.setOnClickListener {
                    if (model.validForRefercode.get()) {
                        mRedeemCouponDialog?.dismiss()
                        this@FragmentWallet.viewModel.redeemCode(model.coupon)
                    } else {
                        dialogView.inputLayout1.error =
                            getString(R.string.invalid_wallet_redeem_code)
                    }
                }
            }
        }
    }

    private fun performBonusClick() {
        startActivity(
            Intent(activity, CommonFragmentActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "recent")
                .putExtra("tab", 1)
                .putExtra(
                    "currentBalance",
                    viewModel.walletInfo.value?.Balance?.TotalAmount ?: 0.0
                )
        )
    }

    override fun onAddressNotVerified() {
        launchAddressVerificationScreen(viewModel.addressVerificationRejectMsg)
    }

    override fun performOnAddCashClick() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return
        viewModel.analyticsHelper.apply {
            addTrigger(AnalyticsKey.Screens.Wallet,AnalyticsKey.Screens.AddCash)
            fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AddCash,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Wallet
                )
            )
        }

        startActivityForResult(
            Intent(activity, RummyAddCashActivity::class.java)
                .putExtra("wallet", viewModel.walletInfo.value?.Balance)
                .putExtra(MyConstants.INTENT_PASS_AMOUNT, viewModel.walletInfo.value?.AddCashAmount)
                .putExtra(MyConstants.INTENT_PASS_IS_ADDRESS_VERIFICATION_REQUIRED,viewModel.isAddressVerified)
                .putExtra(MyConstants.INTENT_PASS_VERIFICATION_REJECT_MSG,viewModel.addressVerificationRejectMsg)
                .putExtra(
                    "currentBalance",
                    viewModel.walletInfo.value?.Balance?.TotalAmount ?: 0.0
                ), REQUEST_UPDATE_VERIFY_DETAILS
        )
    }

    override fun hideKeyboard() {
        activity?.window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(container.windowToken, 0)
    }

    //fill teamnane dob etc for new users.
    override fun fillProfileDataForFirstTimeUser() {
        val dialogView: DialogTeamProfileformRummyBinding? = DataBindingUtil.inflate(
            layoutInflater, R.layout.dialog_team_profileform_rummy,
            null, false
        )
        activity?.let { activity ->
            dialogView?.run {
                val mDialogProfileForm =
                    MyDialog(activity).getMyDialog(dialogView.root)
                mDialogProfileForm.setCancelable(false)
                date = viewModel.date
                pincode = viewModel.pincode
                colorCode = viewModel.selectedColor.get()


                editDOB.addTextChangedListener {
                    if (it.toString().isNotEmpty()) {
                        inputdob.error = getString(R.string.our_platform_requires_minimum_age_18)
                    }
                }

                editDOB.setOnClickListener {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        activity,
                        R.style.RummySdkDatePickerDialogTheme,
                        DatePickerDialog.OnDateSetListener { p0, p1, p2, p3 ->
                            val dob =
                                p3.toString() + "/" + (p2 + 1).toString() + "/" + p1.toString()
                            editDOB.setText(dob)
                            viewModel.date.set(dob)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 18)
                        val date = Date(cal.timeInMillis)
                        datePicker.maxDate = date.time
                    }.show()
                }

                txtNo.setOnClickListener {
                    mDialogProfileForm.dismiss()
                    viewModel.resetFormData()
                }

                txtYes.setOnClickListener {
                    if (viewModel.date.get().isNullOrEmpty()) {
                        dialogView.inputdob.error = "PLease Enter your Date of Birth"
                    } else if (!validPinCode(viewModel.pincode.get() ?: ""))
                        dialogView.inputpinCode.error = "PLease Enter Valid PinCode"
                    else {
                        viewModel.updateProfileFirstTimeUser()
                        mDialogProfileForm.dismiss()
                    }
                }

                mDialogProfileForm.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.isLoading.set(false)
        if (requestCode==REQUEST_UPDATE_VERIFY_DETAILS) {

            viewModel.updateLoginModel()
        }
        if (REQUEST_CODE_UPDATE_WALLET==requestCode)
        if (resultCode == Activity.RESULT_OK)
            viewModel.getScratchCard()

        if (resultCode == MyConstants.GAME_RELOAD){
            (activity as? RummyMainActivity)?.redirectToTab(R.id.navigation_home)
        }

        if(requestCode == REQUEST_CODE_UPDATE_WALLET && resultCode == Activity.RESULT_OK){
            //showMessage("Fund added successfully")
        }
    }

    fun showShowcase() {
        if (!viewModel.prefs.isWalletShowCaseNew) {
            activity?.let {
                val bubbleListener = object : BubbleShowCaseListener {
                    override fun onTargetClick(bubbleShowCase: BubbleShowCase) {
                        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
                    }

                    override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) {
                        isBubbleShowcaseVisible = false
                        binding.scrollView.post {
                            binding.scrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    }

                    override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) {
                    }

                    override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {
                        binding.scrollView.post {
                            binding.scrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                }

                isBubbleShowcaseVisible = true
                if (viewModel.isAvtivity.get())
                    binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }

                mShowCaseBuilder = BubbleShowCaseBuilder(it)
                    .arrowPosition(BubbleShowCase.ArrowPosition.TOP)
                    .textColor(Color.BLACK)
                    .disableTargetClick(true)
                    .disableCloseAction(false)
                    .titleTextSize(15)
                    .descriptionTextSize(13)
                    .setHideSkipbutton(true)
                    .listener(bubbleListener)
                    .showOnce(Withdrawal_Wallet_Section)
                    .title(getString(R.string.showcase_withdrawal))
                    .description(getString(R.string.showcase_withdrawal_description))
                    .targetView(binding.btnWithdrawMoney)

                val b = BubbleShowCaseSequence()
                b.setSkipListener(this)
                b.addShowCase(mShowCaseBuilder!!)
                b.show()
            }
        }
    }

    fun disMissShowCase(){
        viewModel.prefs.isWalletShowCaseNew = false
        mShowCaseBuilder?.dismiss()
    }

    override fun onSkipClick() {
        isBubbleShowcaseVisible = false
        viewModel.prefs.isWalletShowCaseNew = true
        arguments?.getString(MyConstants.INTENT_PASS_OPEN_REDDEM)?.let {
            if (it != "") {
                viewModel.isBottomSheetVisible.set(true)
                viewModel.availableCode.set(it)
            }
        }
    }

    override fun onNextClick() {}

    private fun <F> addFragment(fragment: F) where F : Fragment, F : MainNavigationFragment {
        activity?.supportFragmentManager?.inTransaction {
            add(RummyMainActivity.FRAGMENT_ID, fragment).addToBackStack(null)
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        swipeRefresh.isRefreshing = false
        println(throwable?.message.toString())
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        swipeRefresh.isRefreshing = false
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        swipeRefresh.isRefreshing = false
    }

    override fun showMessage(message: String?) {
        swipeRefresh.isRefreshing = false
        message?.let { showMessageView(it) }
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)


    private fun setGraph(walletInfoData: WalletInfoModel) {
        val l = piechart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.textSize = 0f
        l.isEnabled = false

        val graphValues = ArrayList<PieEntry>()
        val colorArrayList = ArrayList<Int>()

        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Winning
                ), "Type1"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Unutilized
                ), "Type2"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Bonus
                ), "Type3"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.DailyBonus
                ), "Type4"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.SignUpBonus
                ), "Type5"
            )
        )

        colorArrayList.add(Color.parseColor("#3ecb87"))

        colorArrayList.add(Color.parseColor("#e79556"))
        colorArrayList.add(Color.parseColor("#877aef"))
        colorArrayList.add(Color.parseColor("#373737"))
        colorArrayList.add(Color.parseColor("#373737"))

        val dataSet = PieDataSet(graphValues, "")
        dataSet.sliceSpace = 0f
        val data = PieData(dataSet)
        piechart.isDrawHoleEnabled = true
        piechart.transparentCircleRadius = 0f
        piechart.holeRadius = 85f
        dataSet.setColors(*colorArrayList.toIntArray())
        data.setValueTextSize(1f)
        data.setValueTextColor(Color.TRANSPARENT)
        piechart.animateXY(1400, 1400)
        piechart.setEntryLabelColor(Color.TRANSPARENT)
        piechart.highlightValues(null)
        data.setValueFormatter(PercentFormatter())
        piechart.data = data
        piechart.description.isEnabled = false
        piechart.setHoleColor(ContextCompat.getColor(requireContext(),R.color.black))


    }

    private fun getPercentValue(total: Double, amount: Double) = (amount * 100 / total).toFloat()

    override fun performBonusListClick(model: WalletInfoModel.WalletBonesModel) {
        if (model.walletType == 2) {
            arguments?.getBoolean("isActivity")?.let {
                if (it)
                    addFragment(FragmentCashBonus.newInstance(it))
                else startActivity(
                    Intent(requireActivity(), CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "CashBonus"))
            }
        }
        if(model.walletType == 1 && viewModel.bonusSubList.size >=2){
            viewModel.isGstBonusShow.set(!viewModel.isGstBonusShow.get())
        }
    }

}

interface WalletNavigator {
    fun fillProfileDataForFirstTimeUser()
    fun performOnAddCashClick()
    fun onAddressNotVerified()
    fun performBonusListClick(model : WalletInfoModel.WalletBonesModel)
}

interface OnOfferBannerClick {
    fun onOfferClick(offer: HeaderItemModel){}
}

interface HideKeyboardInterface {
    fun hideKeyboard()
}