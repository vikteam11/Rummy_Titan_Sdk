package com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets

import com.rummytitans.playcashrummyonline.cardgame.R
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


open class BaseBottomSheetDialog (context: Context):BottomSheetDialog(context, R.style.BottomSheetDialog){

    init {
        dismissWithAnimation=true
        behavior.apply {
            isDraggable=false
            state= BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun show() {
        if(isShowing)return
        behavior.state=BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }
}

