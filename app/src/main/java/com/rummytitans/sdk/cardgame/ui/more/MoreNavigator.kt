package com.rummytitans.sdk.cardgame.ui.more

interface MoreNavigator {
    fun onWebClick(url: String, titleId: Int)
    fun onItemClick(position: Int)
    fun notificationApiCalled()
}