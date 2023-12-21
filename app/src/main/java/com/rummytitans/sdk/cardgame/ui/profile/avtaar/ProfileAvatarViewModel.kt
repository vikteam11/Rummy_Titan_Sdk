package com.rummytitans.sdk.cardgame.ui.profile.avtaar

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.rummytitans.sdk.cardgame.ui.completeprofile.AvatarViewModel


class ProfileAvatarViewModel(var model: ProfileAvtarItem, var clickListener: AvatarViewModel.AvatarClickListener) {
    var imageUrl = ObservableInt(model.avtarIcon)
    var isSelected = ObservableBoolean(model.selected)

    fun onClick() {
        clickListener.onItemClick()
    }
}