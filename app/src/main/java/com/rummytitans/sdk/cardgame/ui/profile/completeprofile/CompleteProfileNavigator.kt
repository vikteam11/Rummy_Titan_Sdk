package com.rummytitans.sdk.cardgame.ui.completeprofile

interface CompleteProfileNavigator {

    fun saveProfile()

    fun completeProfileSuccess()

    fun fireBranchEvent(eventName: String?, userId: Int)

}