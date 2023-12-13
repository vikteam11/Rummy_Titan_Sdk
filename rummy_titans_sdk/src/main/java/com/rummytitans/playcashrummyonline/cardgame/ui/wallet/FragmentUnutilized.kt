package com.rummytitans.playcashrummyonline.cardgame.ui.wallet

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentUnutilizedRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment


import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.UnutilizedViewModel
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import kotlinx.android.synthetic.main.dialog_success_withdrawal_rummy.*
import kotlinx.android.synthetic.main.fragment_unutilized_rummy.*
import java.text.DecimalFormat
import javax.inject.Inject

class FragmentUnutilized : BaseFragment(), MainNavigationFragment,
    BaseNavigator {

    lateinit var binding: FragmentUnutilizedRummyBinding
    lateinit var mViewmModel: UnutilizedViewModel
    //@Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory
    var backDialog: Dialog? = null

    companion object {
        fun newInstance(currentBalance: Double, widText: String, totalWinning: Double)
                : FragmentUnutilized {
            val frag = FragmentUnutilized()
            val bundle = Bundle()
            bundle.putDouble("currentBalance", currentBalance)
            bundle.putString("widText", widText)
            bundle.putDouble("totalWinnig", totalWinning)
            frag.arguments = bundle
            return frag
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTheme(inflater)
        setLanguage()
        mViewmModel = ViewModelProvider(
            this
        ).get(UnutilizedViewModel::class.java)
        binding =
            FragmentUnutilizedRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
                lifecycleOwner = this@FragmentUnutilized
                viewmodel = mViewmModel
            }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewmModel.apply {
            navigator = this@FragmentUnutilized
            navigatorAct = this@FragmentUnutilized
            myDialog = MyDialog(requireActivity())
            icBack.setOnClickListener { goBack() }
            currentBalance.value =arguments?.getDouble("currentBalance")
            withdrawtext.value = arguments?.getString("widText")
            winnings = arguments?.getDouble("totalWinnig") ?: 0.0
            edtWithdrawAmount.addTextChangedListener {
                if ((it?.toString()?.length ?: 0) > 0) {
                    withdrawAmount.value = it.toString().toInt()
                    edtWithdrawAmount.setSelection(edtWithdrawAmount.text.length)
                    val amount = (currentBalance.value ?: 0.0 - it.toString().toDouble())
                    "₹${currentBalance} - ₹${withdrawAmount} = ₹${calculateValue(
                        amount
                    )}"
                } else {
                    edtWithdrawAmount.setText("0")
                    edtWithdrawAmount.setSelection(edtWithdrawAmount.text.length)
                }
            }
            withdrawAmount.observe(viewLifecycleOwner, Observer {
                val result=currentBalance.value?.toDouble()?.minus(it)?:0.0
                val finalValue=if(result < 0.0 ) 0.0 else result
                amountText.set("₹"+currentBalance.value+" - ₹"+it+" = ₹"+finalValue)
            })
            btnWithdraw.setOnClickListener {
                    when {
                        withdrawAmount.value?.toDouble() ?: 0.0 > 50000 -> showError(R.string.withdraw_money_should_not_exced1)

                         withdrawAmount.value?.toDouble() ?: 0.0 > currentBalance.value?:0.0 -> showError(
                            R.string.withdraw_money_should_not_exced
                        )

                        withdrawAmount.value ?: 0 < 1 -> {
                            val message =
                                getString(R.string.withdraw_money_need_grater_then) + 1  + " " + getString(
                                    R.string.withdraw_money_need_grater_then1
                                )
                            showError(message)
                        }
                        else -> alertOnMoneyWithDrawal()
                    }
            }
        }

    }

    fun calculateValue(value: Double): String {
        val rrr = DecimalFormat("#####0.00").format(value)
        return if (rrr.equals("nan", ignoreCase = true)) ""
        else rrr + ""
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    fun alertOnMoneyWithDrawal() {
     /*   activity?.let {
            if (backDialog == null) {
                backDialog = MyDialog(it).getMyDialog(R.layout.dialog_for_create_team)
                backDialog?.txtMessage1?.visibility = View.GONE
                backDialog?.txtMessage2?.setText(R.string.are_you_sure_withdral_money)
                backDialog?.txtYes?.text = getString(R.string.withdraw)
                backDialog?.view22?.setBackgroundColor(Color.parseColor(mViewmModel.selectedColor.get()))
                backDialog?.txtNo?.setOnClickListener {
                    if (backDialog?.isShowing == true)
                        backDialog?.dismiss()
                }
                backDialog?.txtYes?.setOnClickListener {
                    if (backDialog?.isShowing == true)
                        backDialog?.dismiss()
                    mViewmModel.withdrawUnutilizedAmount()
                }
                backDialog?.show()
            } else
                backDialog?.show()
        }*/
    }

    override fun handleError(throwable: Throwable?) {
    }

    override fun showMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return
        val d = MyDialog(requireActivity()).getMyDialog(R.layout.dialog_success_withdrawal_rummy)
        //d.view22.setBackgroundColor(Color.parseColor(mViewmModel.selectedColor.get()))
        d.show()
        d.setCancelable(false)
        d.txtMessage1.text = message
        d.txtNo.setOnClickListener {
            d.dismiss()
            goBack()
        }
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

}


