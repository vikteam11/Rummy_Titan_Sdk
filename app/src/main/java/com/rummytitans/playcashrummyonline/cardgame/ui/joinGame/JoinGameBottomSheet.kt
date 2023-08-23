package com.rummytitans.playcashrummyonline.cardgame.ui.joinGame

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.BottomSheetJoinGameRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.RummyLobbyModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.games.rummy.RummyWebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JoinGameBottomSheet : BottomSheetDialogFragment(), JoinGameSheetNavigator{
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: JoinContestViewModel
    lateinit var mBinding: BottomSheetJoinGameRummyBinding

    private val stakeId:String by lazy {
        arguments?.getString("stakeId")?:""
    }

    private val lobby: RummyLobbyModel by lazy {
        (arguments?.getSerializable("lobby") as? RummyLobbyModel?)?: RummyLobbyModel()
    }

    companion object {
        fun newInstance(lobby: RummyLobbyModel):JoinGameBottomSheet {
            val frag = JoinGameBottomSheet()
            val bundle = Bundle()
            bundle.putString("stakeId", lobby.StakeId)
            bundle.putSerializable("lobby", lobby)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onAttach(context: Context) {
       // AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this)
            .get(JoinContestViewModel::class.java)
        viewModel.navigatorAct = this
        (requireActivity() as? BaseActivity? )?.let { act->
            viewModel.navigator = act
        }
        viewModel.lobby = lobby
        viewModel.myDialog = MyDialog(requireActivity())

        mBinding = BottomSheetJoinGameRummyBinding.inflate(
            inflater,container,false
        )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            viewModel = this@JoinGameBottomSheet.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        viewModel.confirmLobby(stakeId)

        mBinding.btnJoinContest.setOnClickListenerDebounce {
            performPlayNowAction()
        }
    }

    private fun performPlayNowAction() {
        if(!viewModel.isAddressVerified){
            sendToAddressVerification()
            return
        }
        viewModel.joinConfirmModel.get()?.totalAmountCollect?.let {addCashAmount->
            if(addCashAmount > 0.0){
                sendToAddCash(addCashAmount)
                return
            }
        }
        viewModel.joinGame()
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

    override fun sendToAddCash(amount: Double) {
       /* startActivityForResult(
            Intent(requireActivity(), AddCashActivity::class.java).putExtra(
                MyConstants.INTENT_PASS_AMOUNT, amount
            ).putExtra(MyConstants.INTENT_ADD_CASH_RESTRICTION, false)
                .putExtra(MyConstants.INTENT_ADD_CASH_FOR_JOIN, true),
            MyConstants.REQUEST_CODE_ADD_CASH
        )*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MyConstants.REQUEST_CODE_ADD_CASH){
            viewModel.confirmLobby(stakeId)
        }

        if(requestCode==MyConstants.REQUEST_UPDATE_VERIFY_DETAILS){
            if (resultCode==Activity.RESULT_OK) {
                viewModel.isAddressVerified = true
                viewModel.confirmLobby(stakeId)
            } else
                dismiss()
        }
    }

    override fun onSuccess(webUrl: String) {
        dismiss()
        startActivity(
            Intent(activity, RummyWebViewActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_WEB_URL, webUrl)
        )
        requireActivity().overridePendingTransition(0,0)
    }

    override fun showError(message: String?) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(activity).enableVibration(false).setText(message?:"")
            .setBackgroundColorRes(R.color.error_red)
            .setOnHideListener { {} }.show()
    }

    override fun sendToAddressVerification() {
        //launchAddressVerificationScreen("")
    }
}

interface JoinGameSheetNavigator {
    fun sendToAddCash(amount: Double)
    fun sendToAddressVerification()
    fun onSuccess(webUrl:String)
    fun showError(message: String?){}

}
