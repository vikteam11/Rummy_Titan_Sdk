package com.rummytitans.playcashrummyonline.cardgame.ui.more

interface MoreNavigator {
    fun onWebClick(url: String, titleId: Int)
    fun onItemClick(position: Int)
    fun notificationApiCalled()
}