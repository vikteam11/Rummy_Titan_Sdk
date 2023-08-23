package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.models.TransactionModel

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
