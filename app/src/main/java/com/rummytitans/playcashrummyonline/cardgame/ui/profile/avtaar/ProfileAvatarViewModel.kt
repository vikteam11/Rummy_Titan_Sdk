package com.rummytitans.playcashrummyonline.cardgame.ui.profile.avtaar

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.rummytitans.playcashrummyonline.cardgame.ui.completeprofile.AvatarViewModel
import dagger.hilt.android.lifecycle.HiltViewModel


class ProfileAvatarViewModel(var model: ProfileAvtarItem, var clickListener: AvatarViewModel.AvatarClickListener) {
    var imageUrl = ObservableInt(model.avtarIcon)
    var isSelected = ObservableBoolean(model.selected)

    fun onClick() {
        clickListener.onItemClick()
    }
}