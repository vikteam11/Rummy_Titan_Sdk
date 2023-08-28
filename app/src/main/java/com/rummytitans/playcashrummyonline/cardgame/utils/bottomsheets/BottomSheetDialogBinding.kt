package com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets

import com.rummytitans.playcashrummyonline.cardgame.R
import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


 class BottomSheetDialogBinding<D:ViewDataBinding> (context: Context,
                                                    @LayoutRes val layoutRes:Int):BottomSheetDialog(context, R.style.RummySdk_BottomSheetDialog){

    var binding: D =
        DataBindingUtil.inflate(LayoutInflater.from(context),layoutRes,null,false)

    init {
        dismissWithAnimation=true
        behavior.apply {
            isDraggable=true
            state= BottomSheetBehavior.STATE_EXPANDED
        }
        setContentView(binding.root)
    }

    override fun show() {
        if(isShowing)return
        behavior.state=BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }
}

