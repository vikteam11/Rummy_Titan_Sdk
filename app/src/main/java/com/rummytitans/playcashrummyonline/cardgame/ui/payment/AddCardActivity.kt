package com.rummytitans.playcashrummyonline.cardgame.ui.payment

import `in`.juspay.services.HyperServices
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyAddCardBinding
import com.rummytitans.playcashrummyonline.cardgame.juspay.JuspayPaymentHelper
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.payment.viewmodel.AddCardViewModel
import com.rummytitans.playcashrummyonline.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.showToolTip
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.playcashrummyonline.cardgame.juspay.PaymentListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rummy_add_card.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddCardActivity : BaseActivity(), AddCardNavigator, PaymentListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var progressDialog: Dialog
    lateinit var binding: ActivityRummyAddCardBinding
    lateinit var viewModel: AddCardViewModel
    private val CARD_NUMBER_TOTAL_SYMBOLS = 19 // size of pattern 0000-0000-0000-0000
    private val CARD_NUMBER_TOTAL_DIGITS = 16 // max numbers of digits in pattern: 0000 x 4
    private val CARD_NUMBER_DIVIDER_MODULO =
        5 // means divider position is every 5th symbol beginning with 1
    private val CARD_NUMBER_DIVIDER_POSITION =
        CARD_NUMBER_DIVIDER_MODULO - 1 // means divider position is every 4th symbol beginning with 0
    private val CARD_NUMBER_DIVIDER = ' '
    var currentYear = 0
    var currentMonth = 0

    lateinit var paymentHelper: JuspayPaymentHelper
    lateinit var hyperInstance: HyperServices

    lateinit var dialogProgress: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddCardViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_add_card)
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_progress_rummy)
        progressDialog.setCancelable(false)
        binding.viewModel = viewModel
        viewModel.navigator = this
        viewModel.navigatorAct = this
        binding.lifecycleOwner = this
        dialogProgress = MyDialog(this).getMyDialog(R.layout.dialog_progress_rummy)
        dialogProgress.setCancelable(false)
        checkCardDetails()
        binding.ivHelpCvv.setOnClickListener {
            showToolTip(this, it, getString(R.string.enter_cvv_message_hint))
        }

        viewModel.checked.set(true)
        viewModel.amount.set(intent?.getDoubleExtra(MyConstants.INTENT_PASS_AMOUNT, 0.0) ?: 0.0)

        editCardNumber.addTextChangedListener {
            if (!isInputCorrect(it!!)) {
                it.replace(
                    0, it.length, concatString(
                        getDigitArray(it, CARD_NUMBER_TOTAL_DIGITS),
                        CARD_NUMBER_DIVIDER_POSITION, CARD_NUMBER_DIVIDER
                    )
                )
            }
        }

        editDate.doOnTextChanged { text, start, removed, added ->
            if (start == 1 && start+added == 2 && text?.contains('/') == false) {
                editDate.setText("$text/")
            } else if (start == 2 && start-removed == 1) {
                editDate.setText(text?.substring(0,1))
            }
            editDate.setSelection(editDate.length())

            if(editDate.length()==5){
                editTextCVV.requestFocus()
            }
        }

        val cal = Calendar.getInstance()
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH)

        ivSupport.setOnClickListener {
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )
        }

        abstract class LocalOnItemSelectedListener : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spExtYear.onItemSelectedListener = object : LocalOnItemSelectedListener() {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.selectedYear = viewModel.yearList.value?.get(position)?.toInt() ?: 0
                if (viewModel.selectedYear == currentYear) spExtMonth.setSelection(currentMonth)
            }
        }

        spExtMonth.onItemSelectedListener = object : LocalOnItemSelectedListener() {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.selectedMonth = viewModel.monthList.get()?.get(position)?.toInt() ?: 0
            }
        }

        binding.editCardNumber.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                binding.ilEditCardNumber.error = null
                viewModel.isMestroCard.set(false)
                viewModel.cardValidImage.set(0)
            }
        }
        binding.editTextCVV.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutCVV.error = null
            showPayButton()
        }
        binding.editTextName.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutName.error = null
            showPayButton()
        }
        setupExpArray()
        setupExpMonthArray()

        hyperInstance = HyperServices(this)
        paymentHelper = JuspayPaymentHelper(this, hyperInstance, this,viewModel.connectionDetector)

        getPaymentInfo()
        binding.executePendingBindings()
    }

    fun showPayButton(){
        val flag1 = if(viewModel.isMestroCard.get())true else binding.editTextCVV.text.toString().length == viewModel.cardLength.get()
        val flag2 = binding.editTextName.text.toString().isNotEmpty()
        viewModel.showPayButton.set(flag1 &&  flag2)
    }

    fun pay(view: View) {
        goForPay()
    }

    private fun getPaymentInfo() {
        (intent?.getSerializableExtra(MyConstants.INTENT_PASS_JUSPAY) as? NewPaymentGateWayModel.JusPayData)?.let {
            paymentHelper.initJuspay(it.CustomerId, it.OrderId, it.Token)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        kotlin.runCatching {
            hyperInstance.onActivityResult(requestCode, resultCode, data!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupExpArray() {
        val years = ArrayList<String>()
        for (i in 0..10) years.add((currentYear + i).toString())
        viewModel._yearList.value = years
    }

    private fun setupExpMonthArray() {
        val months = ArrayList<String>()
        for (i in 0..11) months.add((i + 1).toString())
        viewModel.monthList.set(months)
    }

    private fun isInputCorrect(s: Editable): Boolean {
        var isCorrect = s.length <= CARD_NUMBER_TOTAL_SYMBOLS
        for (i in s.indices) {
            isCorrect =
                if (i > 0 && (i + 1) % CARD_NUMBER_DIVIDER_MODULO == 0) isCorrect and (CARD_NUMBER_DIVIDER == s[i])
                else isCorrect and Character.isDigit(s[i])
        }
        return isCorrect
    }

    private fun concatString(digits: CharArray, dividerPosition: Int, divider: Char): String {
        val formatted = StringBuilder()
        for (i in digits.indices) {
            if (digits[i].toInt() != 0) {
                formatted.append(digits[i])
                if (i > 0 && i < digits.size - 1 && (i + 1) % dividerPosition == 0)
                    formatted.append(divider)
            }
        }
        return formatted.toString()
    }

    private fun getDigitArray(s: Editable, size: Int): CharArray {
        val digits = CharArray(size)
        var index = 0
        var i = 0
        while (i < s.length && index < size) {
            val current = s[i]
            if (Character.isDigit(current)) {
                digits[index] = current
                index++
            }
            i++
        }
        return digits
    }

    @SuppressLint("CheckResult")
    fun checkCardDetails() {
        RxTextView.afterTextChangeEvents(editCardNumber).skipInitialValue()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.editable().toString().length > 12) {
                    viewModel.checkCardDetail(it.editable().toString())
                } else if (viewModel.cardValidImage.get() != 0) {
                    viewModel.cardValidImage.set(0)
                }
            }
    }

    override fun onInvalidCardNumber() {
        binding.ilEditCardNumber.error = getString(R.string.please_enter_a_valid_card_number)
    }

    override fun goForPay() {
        val number = editCardNumber.text.toString()
        if (!viewModel.isCardValid.get()) {
            if (number.length > 12) viewModel.checkCardDetail(number, true)
            else onInvalidCardNumber()
        } else if (editTextCVV.text.toString().length != viewModel.cardLength.get()) {
            binding.inputLayoutCVV.error = getString(R.string.please_enter_correct_cvv_code)
        } else if (TextUtils.isEmpty(editTextName.text.toString())) {
            binding.inputLayoutName.error = getString(R.string.please_enter_name_on_card)
        } else {
            val dateYear = binding.editDate.text.toString().trim().split("/")
            if(dateYear.size ==2){
                viewModel.selectedMonth = dateYear[0].toInt()
                viewModel.selectedYear = dateYear[1].toInt()
            }
            if(viewModel.checked.get()){
                viewModel.saveCard(
                    number.replace(" ", ""),
                    editTextName.text.toString(),
                )
            }else{
                startPayment()
            }
        }
    }

    override fun startPayment() {
        super.startPayment()
        paymentHelper.startNewCardPayment(
            editCardNumber.text.toString().replace(" ", ""),
            viewModel.selectedMonth.toString(),
            viewModel.selectedYear.toString(),
            editTextCVV.text.toString(),
            viewModel.checked.get()
        )
    }
    override fun onBackPressed() {
        val backPressHandled = hyperInstance.onBackPressed()
        if (!backPressHandled) {
            if(viewModel.isNewCardSaved){
                val intent = Intent()
                intent.putExtra("newCard",true)
                setResult(Activity.RESULT_OK,intent)
            }
            super.onBackPressed()
        }
    }

    override fun onPaymentStatusReceived(paymentStatus: Boolean,reason: String?) {
        if (paymentStatus)
            onPaymentSuccess()
        else
            onPaymentFailure(reason?:"")
    }

    override fun onPaymentSuccess() {
        fireOnPaymentDoneEvent(true)
     //   Toast.makeText(this, "Payment Success", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onPaymentFailure(reason: String) {
        fireOnPaymentDoneEvent(false)
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
    }

    override fun showLoader() {
        println("Juspay ---> Show Loader")
        if (!dialogProgress.isShowing) dialogProgress.show()
    }

    override fun hideLoader() {
        println("Juspay ---> Hide Loader")
        if (dialogProgress.isShowing) dialogProgress.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        hyperInstance.terminate()
    }

    private fun fireOnPaymentDoneEvent(status: Boolean) {
        val comingFor = intent.getStringExtra(PaymentOptionActivity.COMING_FOR) ?: ""
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.PaymentDone, bundleOf(
                AnalyticsKey.Keys.Amount to viewModel.amount.get()?.toInt(),
                AnalyticsKey.Keys.Gateway to "",
                AnalyticsKey.Keys.Type to "Card",
                AnalyticsKey.Keys.IsOffer to "No",
                AnalyticsKey.Keys.GoldenTicket to
                        if (comingFor.equals(PaymentOptionActivity.COUPON, ignoreCase = true))
                            "Yes" else "No",
                AnalyticsKey.Keys.Status to if (status) "Success" else "Fail",
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Payment
            )
        )
    }

    override fun onRequestPermissionsResult(code: Int, perm: Array<out String>, result: IntArray) {
        hyperInstance.onRequestPermissionsResult(code, perm, result)
        super.onRequestPermissionsResult(code, perm, result)
    }
}

interface AddCardNavigator {
    fun goForPay()
    fun startPayment(){}
    fun onInvalidCardNumber()
}