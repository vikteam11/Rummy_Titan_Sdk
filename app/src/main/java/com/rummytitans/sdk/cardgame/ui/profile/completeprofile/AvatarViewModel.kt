package com.rummytitans.sdk.cardgame.ui.completeprofile

import com.rummytitans.sdk.cardgame.models.AvatarModel
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import dagger.hilt.android.lifecycle.HiltViewModel


class AvatarViewModel(var model: AvatarModel, var clickListener: AvatarClickListener) {
    var imageUrl = ObservableInt(model.imageUrl)
    var isSelected = ObservableBoolean(model.isSelected)

    fun onClick() {
        clickListener.onItemClick()
    }

    interface AvatarClickListener {
        fun onItemClick()
    }
}