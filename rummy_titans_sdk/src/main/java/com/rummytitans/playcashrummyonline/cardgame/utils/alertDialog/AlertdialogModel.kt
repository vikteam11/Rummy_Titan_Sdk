package com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog

const val ALERT_DIALOG_POSITIVE:Byte=1
const val ALERT_DIALOG_NEGETIVE:Byte=2
const val ALERT_DIALOG_EMPTY:Byte=0

class AlertdialogModel(val title:String="",
                       val description:String="",
                       val negativeText:String="",
                       val positiveText:String="",
                       val onNegativeClick:()->Unit={},
                       val onPositiveClick:()->Unit={},
                       val imgRes:Int=0, val imgUrl:String="",
                       val onCloseClick:()->Unit={},
                       var showClose:Boolean = false,
                       val isDefaultImgPositive:Byte= ALERT_DIALOG_EMPTY)