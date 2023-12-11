package com.rummytitans.sdk.cardgame.ui.payment

class CardItemViewModel constructor(
    val card: PaymentModel.AddCard,
    val listener: CardListener,
    val showLine: Boolean
)

interface CardListener {
    fun deleteCard()
    fun cardClick()
    fun cardPay()
}