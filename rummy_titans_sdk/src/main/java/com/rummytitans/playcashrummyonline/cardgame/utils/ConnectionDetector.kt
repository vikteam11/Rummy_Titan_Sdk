package com.rummytitans.playcashrummyonline.cardgame.utils

import com.rummytitans.playcashrummyonline.cardgame.MainApplication
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


class ConnectionDetector(internal var context: Context) {

    val isConnected: Boolean
        get() {
            return isOnline()
        }

    private fun isOnline(): Boolean {
        val app = (context as? MainApplication)
        if (app?.isAppBackgroud == true) {
            return true
        }
        if(app?.isFromBackground==true){
            Thread.sleep(500)
        }
        app?.isFromBackground  =false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnectedOrConnecting
        }
    }
}
