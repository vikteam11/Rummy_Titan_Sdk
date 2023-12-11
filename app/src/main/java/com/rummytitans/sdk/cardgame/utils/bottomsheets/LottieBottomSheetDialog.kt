package com.rummytitans.sdk.cardgame.utils.bottomsheets


import com.rummytitans.sdk.cardgame.utils.bottomsheets.listeners.BottomSheetStatusListener
import com.rummytitans.sdk.cardgame.databinding.BottomsheetDialogLottieRummyBinding
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import android.content.Context
import android.view.LayoutInflater


class LottieBottomSheetDialog(
    context: Context,
    var statusDataModel: BottomSheetStatusDataModel,
    val listener:BottomSheetStatusListener?=null)
    : BaseBottomSheetDialog(context){

    var binding: BottomsheetDialogLottieRummyBinding =
        BottomsheetDialogLottieRummyBinding.inflate(LayoutInflater.from(context),null,false)

    init {
        binding.model = statusDataModel
        setContentView(binding.root)
        setCancelable(statusDataModel.cancelAble)
        setCanceledOnTouchOutside(statusDataModel.cancelAble)
        renderView()
    }

    private fun renderView() {
        binding.imgCross.setOnClickListenerDebounce {
            dismiss()
            if(statusDataModel.isSuccess){
                listener?.onVerificationSuccess()
            }else{
                listener?.onVerificationFailed()
            }
        }

        binding.btnSubmitDone.setOnClickListenerDebounce {
            dismiss()
            if(statusDataModel.isSuccess){
                listener?.onVerificationSuccess()
            }else{
                listener?.onVerificationFailed()
            }
        }
    }

    fun updateData(statusDataModel: BottomSheetStatusDataModel){
        setCancelable(true)
        this.statusDataModel = statusDataModel
        binding.invalidateAll()
        binding.model = statusDataModel
        binding.executePendingBindings()
    }

    override fun onBackPressed() {
        if (!statusDataModel.loading){
            dismiss()
            if(statusDataModel.isSuccess){
                listener?.onVerificationSuccess()
            }else{
                listener?.onVerificationFailed()
            }
        }
    }
}
