package com.rummytitans.playcashrummyonline.cardgame.ui.profile.avtaar

data class ProfileAvtarModel(
    val title:String ="",
    val avtarList:ArrayList<ProfileAvtarItem>,
    val isNewAvtar:Boolean=false
){
    fun isAvtarSelected() = avtarList.any { it.selected }
}

data class ProfileAvtarItem(
    val avtarIcon:Int =0,
    val avtarId:Int =0,
    var selected:Boolean=false
)