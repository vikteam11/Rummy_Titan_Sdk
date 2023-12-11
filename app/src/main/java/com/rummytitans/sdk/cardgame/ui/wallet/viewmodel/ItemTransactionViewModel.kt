package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import com.rummytitans.sdk.cardgame.models.TransactionModel

class ItemTransactionViewModel(
    var data: TransactionModel.TransactionListModel,
    val tabName: String,
    val asyncRequestData: () -> Unit,
    val onToggleView: () -> Unit,
    val colorCode: String,
    val sendToWithdrawalDetail: () -> Unit = {}
) {
    fun expandCollapse() {
        if (!data.isDetailAvailable) asyncRequestData() else onToggleView()
    }

}
