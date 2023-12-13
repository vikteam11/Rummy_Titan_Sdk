package com.rummytitans.playcashrummyonline.cardgame.ui.payment

import `in`.juspay.services.HyperServices
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyPaymentOptionBinding
import com.rummytitans.playcashrummyonline.cardgame.juspay.JuspayPaymentHelper
import com.rummytitans.playcashrummyonline.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.playcashrummyonline.cardgame.models.WalletInfoModel
import com.rummytitans.playcashrummyonline.cardgame.ui.WebPaymentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.payment.adapter.*
import com.rummytitans.playcashrummyonline.cardgame.ui.payment.viewmodel.PaymentOptionViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.AnalyticsEventsKeys
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.validUpi
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.playcashrummyonline.cardgame.juspay.PaymentListener
import com.rummytitans.playcashrummyonline.cardgame.ui.payment.viewmodel.WalletInitializeModel
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.LottieBottomSheetDialog
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.listeners.BottomSheetStatusListener
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.playcashrummyonline.cardgame.utils.otp_read.OtpReceiver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rummy_payment_option.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PaymentOptionActivity : BaseActivity(), PaymentOptionNavigator, BottomSheetStatusListener, PaymentListener,
    OtpReceiver.OTPReceiveListener {

    lateinit var binding: ActivityRummyPaymentOptionBinding
    lateinit var viewModel: PaymentOptionViewModel
    val mOtpReceiver by lazy { OtpReceiver() }

    //@Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory
    private var paymentBy = ""
    private val availableUpi = ArrayList<String>()
    private var disposableSearchText: Disposable? = null
    private var comingFor = ""

    lateinit var paymentHelper: JuspayPaymentHelper
    lateinit var hyperInstance: HyperServices
    lateinit var dialogProgress: Dialog

    private val mPaymentAdapter:PaymentGatewayAdapter by lazy { PaymentGatewayAdapter(
        arrayListOf(),
        this,
        Color.parseColor(viewModel.selectedColor.get())
    ) }

    companion object {
        const val COMING_FOR = "comingFor"
        const val COUPON = "coupon"
        const val ADD_CASH = "addcash"
        const val REQUEST_ADD_CARD=102

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)
            .get(PaymentOptionViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_payment_option)
        binding.viewModel = viewModel
        viewModel.navigatorAct = this
        viewModel.navigator = this
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        dialogProgress = MyDialog(this).getMyDialog(R.layout.dialog_progress_rummy)
        dialogProgress.setCancelable(false)

        comingFor = intent.getStringExtra(COMING_FOR) ?: ""

        if (intent.hasExtra("wallet")) {
            (intent.getSerializableExtra("wallet") as? WalletInfoModel.Balance)?.let {
                viewModel.balanceModel = it
            }
        }

        viewModel.amount.set(intent?.getDoubleExtra(MyConstants.INTENT_PASS_AMOUNT, 0.0) ?: 0.0)

        rvPaymentOptions.adapter = mPaymentAdapter

        hyperInstance = HyperServices(this)
        paymentHelper = JuspayPaymentHelper(this, hyperInstance, this,viewModel.connectionDetector)

        observeData()

        viewModel.addCashCouponID = intent.getIntExtra("passId", 0)
        viewModel.goldenTicketId = intent.getIntExtra("goldenTicketID", 0)
        viewModel.offerIds= intent.getStringExtra("offerIds" )?:""
        if (comingFor.equals(COUPON, ignoreCase = true)) {
            viewModel.getPaymentGateWay()
        } else {
            viewModel.isLoading.set(false)
            viewModel.cardListCount.set(intent.getIntExtra("cardListCount", 0))
            viewModel._mGateWayResponse.value =
                intent.getSerializableExtra("paymentModel") as NewPaymentGateWayModel
        }

        val upiApps = packageManager.queryIntentActivities(
            Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("upi://pay?")), 0
        )
        for (app in upiApps) {
            availableUpi.add(app.activityInfo.packageName)
        }

        ivSupport.setOnClickListener {
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )
        }

        initListener()

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Payment
            )
        )
    }

    private fun startSMSListener() {
        try {
            mOtpReceiver.setOTPListener(this)
            val intentFilter = IntentFilter()
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
            this.registerReceiver(mOtpReceiver, intentFilter)
            val client = SmsRetriever.getClient(this)
            client.startSmsRetriever()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOTPReceived(otp: String?) {
        binding.bottomSheetLinkWallet.otpView.setText(otp)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mOtpReceiver,  IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mOtpReceiver)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
                fireOnPaymentDoneEvent(true)
                finish()
            }
        }
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val newCard = it.getBooleanExtra("newCard",false)?:false
                    if(newCard){
                        viewModel.getPaymentGateWay()
                    }else{
                        onPaymentSuccess()
                    }
                }?: kotlin.run {
                    onPaymentSuccess()
                }
            }
        }
    }

    var paymentType = ""
    var paymentGateway = ""
    var isOffer = false

    private fun fireOnPaymentDoneEvent(status: Boolean) {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.PaymentDone, bundleOf(
                AnalyticsKey.Keys.Amount to viewModel.amount.get()?.toInt(),
                AnalyticsKey.Keys.Gateway to paymentGateway,
                AnalyticsKey.Keys.Type to paymentType,
                AnalyticsKey.Keys.IsOffer to if (isOffer) "Yes" else "No",
                AnalyticsKey.Keys.GoldenTicket to
                        if (comingFor.equals(COUPON, ignoreCase = true)) "Yes" else "No",
                AnalyticsKey.Keys.Status to if (status) "Success" else "Fail",
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Payment
            )
        )

        if (comingFor.equals(COUPON, ignoreCase = true)) {
            if (!viewModel.prefs.firstPassPurchased) {
                viewModel.analyticsHelper.fireAttributesEvent(
                    AnalyticsEventsKeys.FirstPassPurchased,
                    viewModel.loginResponse.UserId.toString()
                )
                viewModel.prefs.firstPassPurchased = true
            }
        }
    }

    private fun observeData() {
        viewModel.mGateWayResponse.observe(this){
            paymentHelper.initJuspay(it.jusPayData.CustomerId, it.jusPayData.OrderId, it.jusPayData.Token)

            it.GatewayList.filter { !it.Disable }.map {gatWayList ->
                /*Show only upi which is installed in device*/
                if(gatWayList.List.isNotEmpty() && gatWayList.Type ==4){
                    val newUpi = ArrayList<NewPaymentGateWayModel.PaymentResponseModel>()
                    for (app in gatWayList.List) {
                        if (availableUpi.contains(app.Packge))
                            newUpi.add(app)
                    }
                    gatWayList.List = newUpi
                }
                gatWayList
            }.let {filteredList->
                mPaymentAdapter.updateItems(filteredList as ArrayList<NewPaymentGateWayModel.GatewayList>)
            }

        }

        viewModel.linkOtpSend.addOnPropertyChangedCallback(object :Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if(viewModel.linkOtpSend.get()){
                    binding.root.postDelayed({
                        binding.bottomSheetLinkWallet.otpView.isFocusable= true
                        binding.bottomSheetLinkWallet.otpView.requestFocus()
                    },500)
                }else{
                    binding.bottomSheetLinkWallet.otpView.clearFocus()
                }
            }
        })

        viewModel.bottomSheetLinkWallet.observe(this){
            if(it == BottomSheetBehavior.STATE_HIDDEN){
                hideKeyboard()
                binding.bottomSheetLinkWallet.otpView.clearFocus()
            }
        }

        viewModel.bottomSheetAddUpi.observe(this){
            if(it == BottomSheetBehavior.STATE_HIDDEN){
                hideKeyboard()
                binding.bottomSheetAddUpi.editUpi.setText("")
                binding.bottomSheetAddUpi.inputUpi.error = null
                binding.bottomSheetAddUpi.editUpi.clearFocus()
            }
        }

        viewModel.isBottomSheetVisible.addOnPropertyChangedCallback(object :Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if(!viewModel.isBottomSheetVisible.get()){
                    binding.bottomSheetAdditionGateways.editTextSearch.clearFocus()
                }
            }
        })
    }

    private fun initListener(){
        binding.bottomSheetLinkWallet.otpView.addTextChangedListener {
            val otp = it.toString()
            viewModel.validForPinview.set(otp.length == 6)
            if (otp.length == 6){
                viewModel.linkedOtp = otp
                binding.bottomSheetLinkWallet.txtLink.performClick()
            }
        }

        binding.bottomSheetLinkWallet.txtLink.setOnClickListener {
            if (viewModel.linkOtpSend.get()) {
                //verify otp
                if (viewModel.validForPinview.get()) {
                    viewModel.authenticateWallet()
                }else {
                    viewModel.wrongOtpErrorMsg.set(getString(R.string.err_invalid_otp))
                }
            }else {
                //send otp
                viewModel.sendOtpForLinkWallet()
            }
        }
    }
    override fun addNewCard() {
        startActivityForResult(
            Intent(this, AddCardActivity::class.java)
                .putExtra(COMING_FOR, comingFor)
                .putExtra(MyConstants.INTENT_PASS_AMOUNT, viewModel.amount.get())
                .putExtra(MyConstants.INTENT_PASS_JUSPAY, viewModel.mGateWayResponse.value?.jusPayData),
            102
        )
    }

    override fun payViaCard(token:String, cvv: String) {
        super.payViaCard(token, cvv)
        if (!TextUtils.isEmpty(cvv)) {
            paymentBy = "payViaCard"
            viewModel.paymentThrought = "payViaCard"
            isOffer = false
            paymentType = "Card"
            paymentGateway = ""
            paymentHelper.startSavedCardPayment(token, cvv)
        }
    }

    override fun payViaUpi(model: NewPaymentGateWayModel.PaymentResponseModel) {
        viewModel.paymentThrought = "payViaUpi"
        paymentBy = model.Name
        isOffer = model.IsOffer
        paymentType = "UPI"
        paymentGateway = model.Name
        paymentHelper.startUPIAppPayment(model.Packge)
    }

    override fun payViaUpiAddress(upiCode: String) {
        if (upiCode.isEmpty()) {
            showError("Enter Upi Address")
        } else if (!validUpi(upiCode)) {
            showError("Enter Valid Upi Address")
        } else {
            paymentBy = "UPIAddress"
            isOffer = false
            paymentType = "UPIAddress"
            paymentGateway = ""
            paymentHelper.startUPIAddressPayment(upiCode)
            viewModel.hideAllSheet()
        }
    }

    override fun payViaNetBanking(model: NewPaymentGateWayModel.PaymentResponseModel) {
        paymentBy = "netBanking"
        isOffer = model.IsOffer
        paymentType = "NetBanking"
        paymentGateway = model.Title
        paymentHelper.startNetBankingPayment(model.Code)
    }

    override fun payViaWallet(model: NewPaymentGateWayModel.PaymentResponseModel) {
        if(model.DirectDebit){
            viewModel.directDebit(model.Token,model.Code)
        }else{
            viewModel.paymentThrought = "payViaWallet"
            paymentBy = model.Title
            isOffer = model.IsOffer
            paymentType = "Wallet"
            paymentGateway = model.Title
            paymentHelper.startWalletPayment(model.Code, "")
        }
    }


    override fun linkWallet(model: NewPaymentGateWayModel.PaymentResponseModel, isLink: Boolean) {
        viewModel.linkedWallet.set(model)
        viewModel.linkOtpSend.set(false)
        viewModel.isLinkWallet.set(isLink)
        if(isLink){
            startSMSListener()
            binding.bottomSheetLinkWallet.otpView.setText("")
            viewModel.toggleLinkWalletSheet()
        }else{
            viewModel.delinkWallet()
        }
    }

    override fun setAdditionPaymentToBottomSheet(
        list: ArrayList<NewPaymentGateWayModel.PaymentResponseModel>?, type: Int
    ) {
        if (!list.isNullOrEmpty()) {
            viewModel.openedGatewayListType = type
            binding.bottomSheetAdditionGateways.editTextSearch.setText("")
            binding.bottomSheetAdditionGateways.recycleWinningBreakup.apply {
                layoutManager = LinearLayoutManager(this@PaymentOptionActivity)
                adapter = AllWalletsAdapter(
                    list, this@PaymentOptionActivity,
                    type, Color.parseColor(viewModel.selectedColor.get())
                )
                disposableSearchText?.dispose()
                if (type == 2) addSearchNameDisposable(list)
            }
        }
    }

    private fun addSearchNameDisposable(list: ArrayList<NewPaymentGateWayModel.PaymentResponseModel>) {
        disposableSearchText = RxTextView
            .afterTextChangeEvents(binding.bottomSheetAdditionGateways.editTextSearch)
            .skipInitialValue()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                it.editable().toString().let { str ->
                    if (TextUtils.isEmpty(str)) {
                        viewModel.isSearchedBankAvailable.set(true)
                        (binding.bottomSheetAdditionGateways.recycleWinningBreakup.adapter as? AllWalletsAdapter)?.apply {
                            listResponse = list
                            type = 2
                            notifyDataSetChanged()
                        }
                    } else {
                        val filteredList = list.filter{
                            it.Title.contains(str, true)
                        }as ArrayList
                        viewModel.isSearchedBankAvailable.set(!filteredList.isNullOrEmpty())
                        (binding.bottomSheetAdditionGateways.recycleWinningBreakup.adapter as? AllWalletsAdapter)?.apply {
                            listResponse = filteredList
                            type = 2
                            notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    override fun openAddUpiDialog() {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AddUPI,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddCash,
            )
        )
        viewModel._bottomSheetAddUpi.value = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomSheetAddUpi.editUpi.addTextChangedListener {
            if (it.toString().isNotEmpty())
                binding.bottomSheetAddUpi.inputUpi.error = null

            viewModel.validUpiCode.set(validUpi(it.toString()))
        }
        binding.bottomSheetAddUpi.txtAdd.setOnClickListener {
            if (viewModel.validUpiCode.get()) {
                val upi = binding.bottomSheetAddUpi.editUpi.text.toString().trim()
                if (upi.contains("@")) {
                    viewModel.verifyUPI(upi)
                } else {
                    binding.bottomSheetAddUpi.inputUpi.error = getString(R.string.error_valid_upiCode)
                }
            } else {
                binding.bottomSheetAddUpi.inputUpi.error = getString(R.string.error_valid_upiCode)
            }
        }
    }

    override fun onBackPressed() {
        if (dialogProgress.isShowing) {
            paymentHelper.cancelPayment()
            dialogProgress.dismiss()
            return
        }

        val backPressHandled = hyperInstance.onBackPressed()
        if (!backPressHandled) {
            when{
                viewModel.isBottomSheetVisible.get()->viewModel.isBottomSheetVisible.set(false)

                viewModel.bottomSheetLinkWallet.value == BottomSheetBehavior.STATE_EXPANDED ->
                    viewModel.toggleLinkWalletSheet()

                viewModel.bottomSheetAlert.value == BottomSheetBehavior.STATE_EXPANDED ->
                    viewModel.showHideAlert()

                viewModel.bottomSheetAddUpi.value == BottomSheetBehavior.STATE_EXPANDED ->
                    viewModel._bottomSheetAddUpi.value = BottomSheetBehavior.STATE_HIDDEN

                else -> super.onBackPressed()
            }
        }
    }

    override fun deleteCard(card: NewPaymentGateWayModel.PaymentResponseModel) {
        viewModel.deleteCardAlert(card.Token)
    }

    override fun onUpdateRequired(message: String) {
        //showAppUpdateDialog(message)
    }

    override fun hideKeyboard() {
        binding.bottomSheetAdditionGateways.bottomSheet.postDelayed({ hideKeyboardView() }, 500)
    }

    override fun onPaymentStatusReceived(paymentStatus: Boolean,reason: String?) {
        if (paymentStatus)
            onPaymentSuccess()
        else
            onPaymentFailure(reason?:"")
    }

    override fun onUpiPaymentResponseReceive(paymentStatus: Int, reason: String?) {
        val dataModel= BottomSheetStatusDataModel().also {
            val (title,des,btnName)= when(paymentStatus){
                JuspayPaymentHelper.PAYMENT_STATUS_FAILED-> {
                    it.btnColorRes=R.color.black
                    it.animationFileId = R.raw.withdrawal_failed_anim
                    listOf(
                        getStringResource(R.string.payment_failed),
                        reason?:getStringResource(R.string.payment_failed_msg),
                        getStringResource(R.string.try_again)
                    )
                }
                JuspayPaymentHelper.PAYMENT_STATUS_PENDING-> {
                    it.btnColorRes=R.color.yellow
                    it.animationFileId = R.raw.transaction_pending_anim
                    listOf(
                        getStringResource(R.string.payment_pending),
                        reason?:getStringResource(R.string.payment_pending_msg),
                        getStringResource(R.string.try_again)
                    )
                }else-> {
                    it.btnColorRes=R.color.alertGreen
                    it.isSuccess=true
                    it.animationFileId =R.raw.withdrawal_done_anim
                    listOf(
                        getStringResource(R.string.payment_success),
                        getStringResource(R.string.payment_succcess_msg),
                        getStringResource(R.string.done)
                    )
                }
            }
            it.title=title
            it.description=des
            it.positiveButtonName=btnName
            it.allowCross=false
        }
        val dialog= LottieBottomSheetDialog(this,dataModel,this)
        dialog.show()
        dialog.setOnDismissListener {
            if (dataModel.isSuccess) {
                fireOnPaymentDoneEvent(true)
                setResult(Activity.RESULT_OK)
                finish()
            }else{
                hideLoader()
                fireOnPaymentDoneEvent(false)
            }
        }
    }

    override fun onPaymentSuccess() {
        //Toast.makeText(this, "Fund added successfully", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        fireOnPaymentDoneEvent(true)
        finish()
    }

    override fun onPaymentFailure(reason: String) {
        hideLoader()
        fireOnPaymentDoneEvent(false)
//        setResult(Activity.RESULT_OK)
//        finish()
        // Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
        viewModel.getPaymentGateWay()
    }


    override fun showLoader() {
        println("Juspay ---> Show Loader")
        if (!dialogProgress.isShowing) dialogProgress.show()
    }

    override fun hideLoader() {
        println("Juspay ---> Hide Loader")
        kotlin.runCatching{ if (dialogProgress.isShowing) dialogProgress.dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        hyperInstance.terminate()
    }

    override fun onRequestPermissionsResult(code: Int, perm: Array<out String>, result: IntArray) {
        hyperInstance.onRequestPermissionsResult(code, perm, result)
        super.onRequestPermissionsResult(code, perm, result)
    }

    override fun showMore(type: Int,list:ArrayList<NewPaymentGateWayModel.PaymentResponseModel>) {
        val btnName = if(type ==2)AnalyticsKey.Values.ViewAllNB else AnalyticsKey.Values.ViewAllWallet
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to btnName,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddCash,
            )
        )
        viewModel.isSearchAllow.set(type == 2)
        viewModel.isSearchedBankAvailable.set(true)
        viewModel.isBottomSheetVisible.set(true)
        setAdditionPaymentToBottomSheet(list, type)
    }

    override fun refreshData(response: WalletInitializeModel?, walletId:String) {

        val model : WalletInitializeModel? = response

        val outerInd = mPaymentAdapter.listResponse.indexOfFirst {
            it.Type == MyConstants.OTHER
        }
        val outerWltInd = mPaymentAdapter.listResponse.indexOfFirst {
            it.Type == MyConstants.WALLET
        }

        if(outerInd >= 0){
            mPaymentAdapter.updateInnerItems(outerInd,model,walletId,viewModel.linkedWallet.get()?.Code?:"")
        }
        if (outerWltInd >=0 ){
            mPaymentAdapter.updateInnerItems(outerWltInd,model,walletId,viewModel.linkedWallet.get()?.Code?:"")
        }

        if(viewModel.isBottomSheetVisible.get()){
            binding.bottomSheetAdditionGateways.recycleWinningBreakup.adapter?.let {
                (it as AllWalletsAdapter).updateItems(model,walletId,viewModel.linkedWallet.get()?.Code?:"")
            }
        }
    }

    /**in case of direct debit
     * Success URL
     * Failed URL
     * web url to open webpage */
    override fun directDebited(url: String) {
        paymentBy = "web"
        startActivityForResult(
            Intent(this, WebPaymentActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
                .putExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR, false)
                .putExtra(MyConstants.INTENT_PASS_FROM_PAYMENTS, true)
                .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, "Payment"), 101
        )
    }

    override fun invokePaytmApp(packageName: String,walletName: String, webUrl: String) {
        kotlin.runCatching {
            val isAvailableInDevice = packageManager?.getPackageInfo(
                packageName, PackageManager.GET_ACTIVITIES
            )?.applicationInfo?.enabled?:false
            Log.e("invokeApp"," $isAvailableInDevice")
            if (isAvailableInDevice)
                paymentHelper.startWalletPayment(walletName,"ANDROID_PAYTM",JuspayPaymentHelper.PAYMENT_TYPE_NATIVE_APP)
            else
                paymentHelper.startWalletPayment(walletName,"","")
        }.onFailure {
            paymentHelper.startWalletPayment(walletName,"","")
        }
    }
}

interface PaymentOptionNavigator {
    fun showMore(type: Int,list:ArrayList<NewPaymentGateWayModel.PaymentResponseModel>) {}
    fun hideKeyboard() {}
    fun addNewCard()
    fun invokePaytmApp(packageName:String,walletName:String, webUrl:String)
    fun openAddUpiDialog() {}
    fun directDebited(url:String) {}
    fun deleteCard(card: NewPaymentGateWayModel.PaymentResponseModel)
    fun refreshData(response:WalletInitializeModel?,walletId:String){}
    fun payViaCard(model: String, cvv: String){}
    fun payViaUpi(model: NewPaymentGateWayModel.PaymentResponseModel)
    fun payViaUpiAddress(upiCode: String)
    fun payViaWallet(model: NewPaymentGateWayModel.PaymentResponseModel)
    fun payViaNetBanking(model: NewPaymentGateWayModel.PaymentResponseModel)
    fun linkWallet(model: NewPaymentGateWayModel.PaymentResponseModel,isLink:Boolean=true){}
    fun onUpdateRequired(message: String)
    fun setAdditionPaymentToBottomSheet(
        list: ArrayList<NewPaymentGateWayModel.PaymentResponseModel>?, type: Int
    )
}