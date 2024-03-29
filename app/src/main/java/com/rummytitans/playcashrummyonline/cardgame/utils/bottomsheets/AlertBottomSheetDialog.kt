package com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes


class AlertBottomSheetDialog(context: Context,
                             @LayoutRes layoutRes:Int,
                             val positiveBtnId:Int=-1,
                             val negativeBtnId:Int=-1,
                             val onPositiveClick:()->Unit={},
                             val onNegativeClick:()->Unit={}) : BaseBottomSheetDialog(context){

    init {
        setCanceledOnTouchOutside(false)
        setContentView(layoutRes)
        onViewRender()
    }

    private fun onViewRender() {
        if (positiveBtnId>-1)
            findViewById<View>(positiveBtnId)?.setOnClickListener {
                onPositiveClick.invoke()
                dismiss()
            }

        if(negativeBtnId >-1){
            findViewById<View>(negativeBtnId)?.setOnClickListener {
                onNegativeClick.invoke()
                dismiss()
            }
        }
    }

    override fun onBackPressed() {
        onNegativeClick.invoke()
        super.onBackPressed()
    }
}
