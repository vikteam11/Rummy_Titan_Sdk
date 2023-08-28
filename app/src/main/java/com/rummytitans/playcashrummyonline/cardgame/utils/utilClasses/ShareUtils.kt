package com.rummytitans.playcashrummyonline.cardgame.utils.utilClasses

import com.rummytitans.playcashrummyonline.cardgame.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

fun createReferLink(referCode: String?, success: (link: String) -> Unit = {}) {
   /* val builder = FirebaseDynamicLinks.getInstance().createDynamicLink()
        .setLink(Uri.parse("https://rummytitans.com/?referCode=${referCode}"))
        .setDomainUriPrefix("https://rummytitans.page.link")
        .setAndroidParameters(
            DynamicLink.AndroidParameters.Builder("com.rummytitans.playcashrummyonline.cardgame")
                .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.rummytitans.playcashrummyonline.cardgame")).build()
        ).setIosParameters(
            DynamicLink.IosParameters.Builder("com.rummytitans.playcashrummyonline.cardgame").setAppStoreId("1221862854").build()
        )
    builder.buildShortDynamicLink().addOnSuccessListener { result ->
        val shortLink = result.shortLink
        success(shortLink.toString())
    }.addOnFailureListener {
        success("")
    }*/
    success("https://rt.m11.io/?referCode=${referCode}")
}

fun Context.shareReferMessage(url: String?, referMessage:String?="",referCode:String?) {
    val  shareDescription=if (TextUtils.isEmpty(referMessage))
        "Hey! Join me on RummyTitans & get up to ₹10000 as a Welcome Bonus! Use referral code:" +
                " $referCode to avail the offer! Download RummyTitans now - $url & gear up for big winnings!"
    else
        referMessage?.replace("$","")?.replace("DeepLinkUrl"," $url ")

    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, shareDescription)
    sendIntent.type = "text/plain"
    startActivity(Intent.createChooser(sendIntent, "Invite Friend - ${getString(R.string.app_name_rummy)}"))
}