package com.rummytitans.sdk.cardgame.utils.bottomsheets

import com.rummytitans.sdk.cardgame.databinding.BottomSheetDialogAlertRummyBinding
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import android.content.Context
import android.view.LayoutInflater


class BottomSheetAlertDialog(context: Context, val alertdialogModel: AlertdialogModel, val colorCode:String="#fff") : BaseBottomSheetDialog(context){
    private lateinit var binding:BottomSheetDialogAlertRummyBinding
    init {
        binding = BottomSheetDialogAlertRummyBinding.inflate(LayoutInflater.from(context),null,false)
        binding.alertModel = alertdialogModel
        setContentView(binding.root)
        onViewRender()
    }

    private fun onViewRender() {
        binding.btnContinue.setOnClickListenerDebounce {
            dismiss()
            alertdialogModel.onPositiveClick()
        }
        binding.txCancel.setOnClickListenerDebounce {
            dismiss()
            alertdialogModel.onNegativeClick()
        }

        binding.ivClose.setOnClickListenerDebounce {
            dismiss()
            alertdialogModel.onCloseClick()
        }
    }
}
