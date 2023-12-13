package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyAddCashBinding
import com.rummytitans.sdk.cardgame.databinding.DialogWalletRedeemCodeRummyBinding
import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.deeplink.DeepLinkActivityRummy
import com.rummytitans.sdk.cardgame.ui.payment.PaymentOptionActivity
import com.rummytitans.sdk.cardgame.ui.wallet.adapter.AvailableCouponsAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.model.WalletRedeemCodeModel
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.AddCashNavigator
import com.rummytitans.sdk.cardgame.ui.wallet.viewmodel.AddCashViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.locationservices.uiModules.CurrentLocationBaseActivity
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.home.adapter.WalletOffersAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.adapter.AddCashBannerAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.adapter.GstCalculationAdapter
import com.rummytitans.sdk.cardgame.ui.wallet.adapter.OffersAdapter
import com.rummytitans.sdk.cardgame.widget.DecimalFilter
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rummy_add_cash.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RummyAddCashActivity :
    CurrentLocationBaseActivity(), OnOfferClick, AddCashNavigator, OnAddCashBannerClick {

    var currentOfferPage = 0
    private var performAddCash=false

    val list = ArrayList<AvailableCouponModel>()
    private var timer: Timer? = null
    var viewScrollingTime: Long = 5

    override fun onUpdateRequired(message: String) {
        //showAppUpdateDialog(message)
    }


    lateinit var binding: ActivityRummyAddCashBinding
    lateinit var viewModel: AddCashViewModel
    var mRedeemCouponDialog: Dialog? = null


    var headerAdapter: AddCashBannerAdapter?=null
    var mOfferAdapter: OffersAdapter?=null
    var couponsAdapter: AvailableCouponsAdapter?=null
    private var addCashRestriction: Boolean = false

    var initialAmount = 0.0

    var isComingForJoin = false

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this
        ).get(AddCashViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_add_cash)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        viewModel.navigator = this
        viewModel.navigatorAct = this
        viewModel.myDialog = MyDialog(this)

        fromGame=intent.getBooleanExtra(MyConstants.INTENT_COME_FROM_GAME,false)

        icBack.setOnClickListener { onBackPressed() }

        ivSupport.setOnClickListener {
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )

        }

        if (intent.hasExtra("currentBalance")) {
            viewModel.currentBalance.set(intent?.getDoubleExtra("currentBalance", 0.0))
            intent?.getBooleanExtra(MyConstants.INTENT_PASS_IS_ADDRESS_VERIFICATION_REQUIRED,true)?.let {isAddressVerified->
                viewModel.isAddressVerified=isAddressVerified
                viewModel.addressVerificationRejectMsg= intent?.getStringExtra(MyConstants.INTENT_PASS_VERIFICATION_REJECT_MSG)?:""
                collectPlayStoreRequiredData()
            }
        } else
            viewModel.getCurentBalance()

        if (intent.hasExtra(MyConstants.INTENT_PASS_AMOUNT)) {
            initialAmount = intent?.getDoubleExtra(MyConstants.INTENT_PASS_AMOUNT, 0.0)?:0.0
            if (initialAmount ?: 0.0 < 10.0) {
                viewModel.addCashAmmount.set(10.0)
            } else
                viewModel.addCashAmmount.set(initialAmount)
        }


        if (viewModel.addCashAmmount.get() != 0.0)
            setAmount(viewModel.addCashAmmount.get())

        addCashRestriction =
            intent?.getBooleanExtra(MyConstants.INTENT_ADD_CASH_RESTRICTION, true) ?: true

        viewModel.getOfferList()

        try {
            if (intent.getSerializableExtra("wallet") is WalletInfoModel.Balance) {
                viewModel.balanceModel =
                    intent.getSerializableExtra("wallet") as WalletInfoModel.Balance
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val amount = intent.getStringExtra("amount") ?: ""

        if (!TextUtils.isEmpty(amount)) {
            if (amount.toIntWithCheck() < 10) setAmount(10.0)
            else setAmount(amount.toDouble())
        }

        if (intent.hasExtra(MyConstants.INTENT_ADD_CASH_FOR_JOIN)) {
            isComingForJoin = intent.getBooleanExtra(MyConstants.INTENT_ADD_CASH_FOR_JOIN, false)
        }

        binding.executePendingBindings()

        initClick()
        initListeners()
        observeData()
        observeGstCalculation()

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddCash,
            )
        )
    }

    private fun callGstData(amount : String,isJoin : Boolean){
        if(!viewModel.enableGstCalculation.get() || amount.toDouble() == 0.0){
            viewModel.isGstCardVisible.set(false)
            return
        }
        viewModel.isGstCardVisible.set(true)
        binding.shimmerViewContainer.startShimmer()
        viewModel.getGstData(amount,isJoin)
    }

    private fun observeGstCalculation(){
        viewModel.gstList.observe(this){gstList->
            binding.shimmerViewContainer.stopShimmer()
            if (gstList.isEmpty()){
                binding.cardTds.visibility= View.GONE
                return@observe
            }else{
                binding.cardTds.visibility= View.VISIBLE
                binding.recycleGst.adapter = GstCalculationAdapter(gstList)
            }
        }
    }


    var fromGame=false
    override fun collectPlayStoreRequiredData() {
        val delayTime=if (fromGame)
            500L
        else 0L
        Handler(mainLooper).postDelayed({
            fromGame=false
            if (viewModel.isLocationRequired())
                showAllowBottomSheet()
            else if(!viewModel.isAddressVerified)//else sequent not user yet.
                onAddressNotVerified()
        },delayTime)

    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if(headerAdapter != null){
            startViewPagerScrolling()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.edtAddAmmount.clearFocus()
    }

    override fun setAmount(amount: Double?) {
        val finalAmount = DecimalFormat("##.##").format(amount)
        binding.edtAddAmmount.setText(if(finalAmount=="0")"" else "$finalAmount")
        binding.edtAddAmmount.setSelection(binding.edtAddAmmount.text.length)
    }

    private fun observeData() {
        viewModel.mBannerOffer.observe(this, Observer {
            headerAdapter = AddCashBannerAdapter(it, this@RummyAddCashActivity)
            binding.viewPagerOffers.adapter = headerAdapter
            binding.tabHeaders.setupWithViewPager(binding.viewPagerOffers)
            startViewPagerScrolling()
            callGstData(viewModel.addCashAmmount.get().toString(),isComingForJoin)
        })

        viewModel.mAddCashOffer.observe(this, Observer {
            mOfferAdapter = OffersAdapter(it,this,viewModel.selectedColor.get())
            binding.rvOffers.adapter = mOfferAdapter
        })

        viewModel.mAvailableCoupons.observe(this, Observer {
            couponsAdapter = AvailableCouponsAdapter(it, viewModel.selectedColor.get(), {
                viewModel.checkCouponCodeAvailability(it)
            }, { viewModel.couponId = 0 }, viewModel.couponAppliedlivedata)
            binding.rvAvailableCoupon.adapter = couponsAdapter
        })
    }

    override fun showDialog(descriptionMsg: String) {
        onRestrictLocationFound(descriptionMsg)
    }

    override fun onAddressNotVerified() {
        if(BuildConfig.isPlayStoreApk == 1 ){
            launchAddressVerificationScreen(viewModel.addressVerificationRejectMsg)
        }
    }

    @SuppressLint("CheckResult")
    private fun initListeners() {
        binding.viewPagerOffers.clipToPadding = false
        binding.viewPagerOffers.pageMargin = 20
        binding.viewPagerOffers.setPadding(40, 10, 40, 10)

        binding.viewPagerOffers.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentOfferPage = position
                startViewPagerScrolling()
            }
        })


        binding.edtAddAmmount.filters = arrayOf(InputFilter.LengthFilter(7),
            DecimalFilter(2)
        )
        RxTextView.afterTextChangeEvents(binding.edtAddAmmount)
            .skipInitialValue()
            .debounce(100, TimeUnit.MICROSECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                try {
                    val amountInString=it.editable().toString()
                    val amount = if (!TextUtils.isEmpty(amountInString)) amountInString.toDouble() else 0.0

                    Log.d("amountInString","amountInString $amountInString")
                    if(amount > 0.0){
                        val amountStr =
                            DecimalFormat("##.##").format(it.editable().toString().toDouble())
                                .toDouble()
                        viewModel.addCashAmmount.set(amountStr)
                        callGstData(amount.toString(),isComingForJoin)
                    }else{
                        clearGstFinalAmount()
                        viewModel.isGstCardVisible.set(false)
                        viewModel.addCashAmmount.set(0.0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    clearGstFinalAmount()
                    viewModel.addCashAmmount.set(0.0)
                }
            }
    }

    private fun clearGstFinalAmount() {
        viewModel.gstModel.value?.finalAmount = 0.0
        viewModel.isGstCardVisible.set(false)
        Log.d("amountInString","clearGstFinalAmount ${viewModel.gstModel.value?.finalAmount}")

    }

    private fun initClick(){
        binding.btnRedeemCoupan.setOnClickListener {
            showRedeemCouponCodeDialog()
        }

        binding.ivClear.setOnClickListener {
            setAmount()
            viewModel.addCashAmmount.set(0.0)

            //remove coupon
            viewModel.couponApplied.value = false
            viewModel.couponId =0

            //unselect all selected offer
            unselectAllOffer()

        }
        binding.btnAddCash.setOnClickListenerDebounce {
            if ((!validAmount(binding.edtAddAmmount.text.toString())
                        || binding.edtAddAmmount.text.toString().toDouble() < if(BuildConfig.DEBUG) 1 else 10)
            ) {
                showError(R.string.oops_minimum_limit_od_add_ammount)
            } else {
                try {
                    performAddCash=true
                    when {
                        viewModel.isLocationRequired() -> {
                            showAllowBottomSheet()
                            return@setOnClickListenerDebounce
                        }
                        BuildConfig.isPlayStoreApk==1 && !viewModel.isAddressVerified-> {
                            onAddressNotVerified()
                            return@setOnClickListenerDebounce
                        }
                    }
                    performAddCash=false
                    startAddCash(binding.edtAddAmmount.text.toString().toDouble())
                } catch (e: Exception) {
                    startAddCash(0.0)
                }
            }
        }
    }


    private fun unselectAllOffer() {
        binding.edtAddAmmount.setFocus(true)
        viewModel.isAllOfferSelected.set(false)
        mOfferAdapter?.listResponse?.map {
            it.isSelected = false
        }
        mOfferAdapter?.notifyDataSetChanged()

    }

    override fun onBackPressed() {
        when {
            isTaskRoot -> {
                startActivity(Intent(this, RummyMainActivity::class.java))
                finish()
            }
            else -> super.onBackPressed()
        }
    }

    private fun startViewPagerScrolling() {
        val numPages = headerAdapter?.count?:0
        val delay = viewScrollingTime
        if (viewModel.allowAutoScrolling && numPages > 1) {
            val handler = Handler(Looper.getMainLooper())
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (currentOfferPage == numPages - 1) currentOfferPage = 0
                        else currentOfferPage++
                        binding.viewPagerOffers.setCurrentItem(currentOfferPage, currentOfferPage!=0)
                    }
                }
            }, delay * 1000, delay * 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==MyConstants.REQUEST_UPDATE_VERIFY_DETAILS){
            if(resultCode==Activity.RESULT_OK) {
                viewModel.apply {
                    isAddressVerified=true
                    addressVerificationRejectMsg=""
                    loginResponse = gson.fromJson(
                        prefs.loginResponse,
                        LoginResponse::class.java
                    )
                }
            }
        }
        if (requestCode == MyConstants.REQUEST_CODE_ADD_CASH) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    fun startAddCash(amount: Double) {
        viewModel.offerIds = mOfferAdapter?.listResponse?.filter { it.isSelected }?.joinToString{ it.Id}?:""
        viewModel.getPaymentMethods(amount)
    }

    override fun goForPayment(response: NewPaymentGateWayModel, amount: Double) {

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AddCashRS,
                AnalyticsKey.Keys.Amount to amount,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddCash
            )
        )

        startActivityForResult(
            Intent(this, PaymentOptionActivity::class.java)
                .putExtra(PaymentOptionActivity.COMING_FOR, PaymentOptionActivity.ADD_CASH)
                .putExtra(MyConstants.INTENT_PASS_AMOUNT, amount)
                .putExtra("paymentModel", response)
                .putExtra("offerIds", viewModel.offerIds)
                .putExtra("passId", viewModel.couponId)
                .putExtra("cardListCount", viewModel.cardListCount.get())
                .putExtra("wallet", viewModel.balanceModel),
            MyConstants.REQUEST_CODE_ADD_CASH
        )

    }

    private fun showRedeemCouponCodeDialog() {
        val dialogView: DialogWalletRedeemCodeRummyBinding? = DataBindingUtil.inflate(
            layoutInflater, R.layout.dialog_wallet_redeem_code_rummy,
            null, false
        )
        val model = WalletRedeemCodeModel(SharedPreferenceStorageRummy(this))

        dialogView?.let {
            dialogView.item = model
            mRedeemCouponDialog = MyDialog(this).getMyDialog(dialogView.root)
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
                    this.viewModel.redeemCode(model.coupon)
                } else {
                    dialogView.inputLayout1.error = getString(R.string.invalid_wallet_redeem_code)
                }
            }
        }

    }

    override fun addAmount(isPlus: Boolean) {

        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)

        val amount = binding.edtAddAmmount.text.toString()
        if (!TextUtils.isEmpty(amount)) {
            var crAmount = (binding.edtAddAmmount.text.toString()).toFloat()

            if (crAmount < 25 && !isPlus) return

            if (crAmount < 500) {
                crAmount = (if (isPlus) crAmount + 100 else crAmount - 25)
                binding.edtAddAmmount.setText("" + crAmount)
                try {
                    viewModel.addCashAmmount.set(crAmount.toDouble())
                } catch (e: Exception) {
                }
            } else if (crAmount >= 500 && crAmount < 1000) {
                crAmount = (if (isPlus) crAmount + 100 else crAmount - 50)
                binding.edtAddAmmount.setText("" + crAmount)
                viewModel.addCashAmmount.set(crAmount.toDouble())
            } else if (crAmount >= 1000 && crAmount < 5000) {
                crAmount = (if (isPlus) crAmount + 200 else crAmount - 100)
                binding.edtAddAmmount.setText("" + crAmount)
                viewModel.addCashAmmount.set(crAmount.toDouble())
            } else if (crAmount >= 5000) {
                crAmount = (if (isPlus) crAmount + 1000 else crAmount - 500)
                binding.edtAddAmmount.setText("" + crAmount)
                viewModel.addCashAmmount.set(crAmount.toDouble())
            } else {
                crAmount = (if (isPlus) crAmount + 100 else crAmount - 100)
                binding.edtAddAmmount.setText("" + crAmount)
                viewModel.addCashAmmount.set(crAmount.toDouble())

            }
        }
    }

    override fun addAmount(ammount: Int) {
        val anyOfferSelected = mOfferAdapter?.listResponse?.any{ it.isSelected }?:false
        if(anyOfferSelected){
            unselectAllOffer()
            setAmount(0.0)
        }
        var crAmount = 0.0
        val amount = binding.edtAddAmmount.text.toString()
        if (validAmount(amount)) crAmount = (binding.edtAddAmmount.text.toString()).toDouble()
        crAmount += ammount
        setAmount(crAmount)
        binding.edtAddAmmount.setSelection( binding.edtAddAmmount.text.length)
        viewModel.addCashAmmount.set(crAmount)
    }

    override fun minusAmount(minusAmount: Int) {
        var crAmount = 0.0
        val amount = binding.edtAddAmmount.text.toString()
        if (validAmount(amount)) crAmount = (binding.edtAddAmmount.text.toString()).toDouble()
        crAmount -= minusAmount
        setAmount(crAmount)
        binding.edtAddAmmount.setSelection( binding.edtAddAmmount.text.length)
        viewModel.addCashAmmount.set(crAmount)
    }

    override fun selectAllOffer(select: Boolean) {
        var amount = 0
        mOfferAdapter?.listResponse?.map {
            amount += it.Add.toInt()
            it.isSelected = select
        }
        if(select){
            setAmount(amount.toDouble())
            hideKeyboardView()
        }else{
            setAmount(amount.toDouble())
        }
        binding.edtAddAmmount.setFocus(!select)
        mOfferAdapter?.notifyDataSetChanged()
    }

    override fun onSelectOffer(position: Int, model: AddCashOfferModel.AddCash) {
        val allOfferSelect = mOfferAdapter?.listResponse?.all { it.isSelected }?:false
        viewModel.isAllOfferSelected.set(allOfferSelect)
        if(allOfferSelect){
            selectAllOffer(true)
        }else{
            val isAnyOfferSelected = mOfferAdapter?.listResponse?.any{ it.isSelected }?:false
            var amount = 0
            mOfferAdapter?.listResponse?.map {
                if(it.isSelected){
                    amount += it.Add.toInt()
                }
            }
            hideKeyboardView()
            setAmount(if(amount > 0)amount.toDouble() else model.Add)
            binding.edtAddAmmount.setFocus(!isAnyOfferSelected)
        }
    }
    override fun onLocationFound(lat: Double, log: Double) {
        viewModel.saveCurrentTime(userLatLog = "$lat,$log")
    }

    override fun onValidLocationFound() {
        if(BuildConfig.isPlayStoreApk==1 && !viewModel.isAddressVerified)
            onAddressNotVerified()
        else if(performAddCash)
            binding.btnAddCash.performClick()
    }

    override fun onBannerClick(offer: WalletInfoModel.Offer) {

    }
}



interface OnAddCashBannerClick {
    fun onBannerClick(offer: WalletInfoModel.Offer){}
}
interface OnOfferClick {
    fun onSelectOffer(position:Int, model: AddCashOfferModel.AddCash)
}