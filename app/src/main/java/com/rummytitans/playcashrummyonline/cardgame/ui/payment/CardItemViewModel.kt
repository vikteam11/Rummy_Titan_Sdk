package com.rummytitans.playcashrummyonline.cardgame.ui.payment

class CardItemViewModel constructor(
    val card: com.rummytitans.playcashrummyonline.cardgame.ui.payment.PaymentModel.AddCard,
    val listener: CardListener,
    val showLine: Boolean
)

interface CardListener {
    fun deleteCard()
    fun cardClick()
    fun cardPay()
}