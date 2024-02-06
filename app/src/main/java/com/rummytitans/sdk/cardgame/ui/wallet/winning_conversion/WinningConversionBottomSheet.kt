package com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion.adapter.WinningConversionBenefitAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion.adapter.WinningConversionRangeAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion.adapter.WinningConversionRangeCallback
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.BottomsheetWinningConversionBinding
import com.rummytitans.sdk.cardgame.models.WinningConversionContentModel
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.utils.formatInString
import com.rummytitans.sdk.cardgame.utils.setAllSpanBold
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class WinningConversionBottomSheet(val callback: WinningConversionCallback):
    BottomSheetDialogFragment(), WinningConversionRangeCallback, WinningConversionNavigator {

    lateinit var viewModel: WinningConversionViewModel
    lateinit var mBinding: BottomsheetWinningConversionBinding
    companion object {
        fun newInstance(totalWinningAmount:Double,
                        callback: WinningConversionCallback
        ): WinningConversionBottomSheet {
            val frag = WinningConversionBottomSheet(callback)
            val bundle = Bundle()
            bundle.putDouble("winnings",totalWinningAmount)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)
            .get(WinningConversionViewModel::class.java)

        (requireActivity() as? RummyMainActivity? )?.let { act->
            viewModel.navigator = act
        }
        viewModel.navigatorAct = this

        viewModel.myDialog = MyDialog(requireActivity())

        mBinding = BottomsheetWinningConversionBinding.inflate(
            inflater,container,false
        )
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return mBinding.root
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dg = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dg.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById(R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.setBackgroundResource(0)
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dg
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            viewModel = this@WinningConversionBottomSheet.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel.totalWinningAmount.set(
            arguments?.getDouble("winnings")?:0.0
        )
        viewModel.getWinningConversionRange()

        initView()
        observeData()
        initClick()

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.WinningConversion
            )
        )
    }

    private fun initView() {

    }

    private fun observeData() {
        viewModel.ranges.observe(viewLifecycleOwner){
            mBinding.inWinningField.rvRange.adapter = WinningConversionRangeAdapter(
                it, viewModel.selectedColor.get() ?:"",this
            )
        }

        viewModel.benefits.observe(viewLifecycleOwner){
            val spanCount = if(it.isNotEmpty())it.size else 1
            mBinding.inWinningHome.rvBenefit.layoutManager =
                GridLayoutManager(requireActivity(),spanCount)
            mBinding.inWinningHome.rvBenefit.adapter = WinningConversionBenefitAdapter(
                it
            )
        }

        viewModel.availedBenefits.observe(viewLifecycleOwner){
            val spanCount = if(it.isNotEmpty())it.size else 1
            mBinding.inWinningComplete.rvBenefit.layoutManager =
                GridLayoutManager(requireActivity(),spanCount)
            mBinding.inWinningComplete.rvBenefit.adapter = WinningConversionBenefitAdapter(
                it
            )
        }
    }

    private fun initClick() {
        initListener()

        mBinding.btnClose.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.Close,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.WinningConversion
                )
            )
            callback.onWinningDeposit()
            dismiss()
        }

        mBinding.inWinningHome.btnWithdraw.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.StillWantToWithdraw,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.WinningConversion
                )
            )
            dismiss()
            callback.sendToWithDraw()
        }

        mBinding.inWinningHome.btnConvert.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.WantToConvert,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.WinningConversion
                )
            )
            viewModel.changeConversionStep(2)
            mBinding.inWinningField.edtAmount.requestFocus()
        }

        mBinding.inWinningField.btnConvert.setOnClickListener {
            if((viewModel.depositAmount.get() ?: 0.0) > 0.0){
                hideKeyboardView()
                mBinding.inWinningField.edtAmount.clearFocus()

                val range =  (mBinding.inWinningField.rvRange.adapter as? WinningConversionRangeAdapter)
                    ?.getSelectedRange()

                viewModel.analyticsHelper.fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ConvertNowDone,
                        AnalyticsKey.Keys.UserWinning to viewModel.totalWinningAmount.get(),
                        AnalyticsKey.Keys.Cbrange to ("${range?.rangeTitle()}"),
                        AnalyticsKey.Keys.Convertedamount to viewModel.selectWinningAmount.get(),
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.WinningConversion
                    )
                )
                viewModel.convertToDeposit()
            }
        }
        mBinding.inWinningField.viewBgEdtLine.setOnClickListener {
            mBinding.inWinningField.edtAmount.showKeyBoard()

        }
    }
    var rxDisposable: Disposable?=null
    fun initListener(){

        rxDisposable =  RxTextView.afterTextChangeEvents(mBinding.inWinningField.edtAmount)
            .skipInitialValue()
            .debounce(750, TimeUnit.MICROSECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                kotlin.runCatching {
                    val amountInString=it.editable().toString()
                    if(!TextUtils.isEmpty(amountInString)){
                        val amount = amountInString.toDouble()
                        if(amount > 0.0){
                            val winning = (viewModel.totalWinningAmount.get() ?: 0.0)
                            if(amount <= winning){
                                checkAmountInRange(amount)
                            }else{
                                viewModel.depositAmount.set(0.0)
                                unSelectRange()
                            }
                        }else{
                            unSelectRange()
                            viewModel.depositAmount.set(0.0)
                        }
                    }else{
                        viewModel.depositAmount.set(0.0)
                    }
                }
            }

        mBinding.inWinningField.edtAmount.addTextChangedListener {
            val amountInString=it?.toString()?:"0.0"
            val maxAm = viewModel.maxAmountToDeposit
            val minAm = viewModel.minAmountToDeposit

            if(!TextUtils.isEmpty(amountInString)){
                val amount = amountInString.toDouble()
                val winning = (viewModel.totalWinningAmount.get() ?: 0.0)
                when {
                    amount <= 0.0 -> {
                        viewModel.depositAmount.set(0.0)
                        displayError("Please enter the valid amount")
                    }
                    amount > winning -> {
                        viewModel.depositAmount.set(0.0)
                        displayError("Please enter an amount less than or equal to your winnings balance")
                    }
                    !(minAm..maxAm).contains(amount) -> {
                        viewModel.depositAmount.set(0.0)
                        displayError("")
                    }
                }
            }else{
                unSelectRange()
                viewModel.depositAmount.set(0.0)
                displayError("")
            }
        }
    }

    private fun displayError(error: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.errorMessage.set(error)
        }
    }
    private fun unSelectRange(){
        (mBinding.inWinningField.rvRange.adapter as? WinningConversionRangeAdapter)
            ?.selectRange(-1)
    }

    private fun checkAmountInRange(amount: Double) {
        lifecycleScope.launch(Dispatchers.IO){
            var match = false
            viewModel.ranges.value?.let {rangeList->
                rangeList.forEachIndexed {index,item->
                    if((item.minAmount..item.maxAmount).contains(amount)) {
                        match = true
                        withContext(Dispatchers.Main) {
                            kotlin.runCatching {
                                (mBinding.inWinningField.rvRange.adapter as? WinningConversionRangeAdapter)
                                    ?.selectRange(index)
                                viewModel.calculateDepositAmount(amount, item.bonusPercentage)
                                mBinding.inWinningField.rvRange.layoutManager?.scrollToPosition(index)
                            }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main){
                if(!match){
                    unSelectRange()
                }else{
                    displayError("")
                }
            }
        }
    }

    fun hideKeyboardView() {
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mBinding.inWinningField.edtAmount.windowToken, 0)
    }

    fun EditText.showKeyBoard(){
        requestFocus()
        isFocusable = true
        kotlin.runCatching {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    override fun onSelectRange(model: WinningConversionContentModel.WinningConversionRangeModel) {
        viewModel.totalWinningAmount.get()?.let {winning->
            val convertAm = if(winning < model.maxAmount){
                winning
            }else{
                model.maxAmount
            }
            setConversionAmount(convertAm.toInt().toDouble())
            viewModel.calculateDepositAmount(convertAm,model.bonusPercentage)
        }
    }

    private fun setConversionAmount(convertAm: Double) {
        val am = convertAm.formatInString()
        mBinding.inWinningField.edtAmount.setText(am)
        mBinding.inWinningField.edtAmount.setSelection(mBinding.inWinningField.edtAmount.length())
    }

    override fun onDestroy() {
        super.onDestroy()
        rxDisposable?.dispose()
    }

    override fun onDismiss(dialog: DialogInterface) {
        callback.onWinningDeposit()
        super.onDismiss(dialog)
    }

    override fun onDepositSuccessfully() {
        viewModel.changeConversionStep(3)
        viewModel.amountDepositedSuccessfully.set(true)
        mBinding.inWinningComplete.lottiSuccess.playAnimation()

        mBinding.inWinningComplete.txtDepositDes.setAllSpanBold(
            viewModel.depositAmountMessage.get()?:"",
            viewModel.depositAmountList
        )
    }

    fun showErrorMessageView(message: String, dismissListener: () -> Unit = { }) {
        if (TextUtils.isEmpty(message))
            return
        //requireActivity().displayToast(message,true)
    }
}

interface WinningConversionNavigator{
    fun onDepositSuccessfully()
}

interface WinningConversionCallback{
    fun onWinningDeposit()
    fun sendToWithDraw()
}