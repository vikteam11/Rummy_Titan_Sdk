package com.rummytitans.playcashrummyonline.cardgame

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue

object AdvertisingIdClient {

    @Throws(Exception::class)
    fun getAdvertisingIdInfo(context: Context): AdInfo? {
        return if (Looper.myLooper() != Looper.getMainLooper()) {
            try {
                context.packageManager.getPackageInfo("com.android.vending", 0)
                val advertisingConnection =
                    AdvertisingConnection()
                val intent = Intent("com.google.android.gms.ads.identifier.service.START")
                intent.setPackage("com.google.android.gms")
                if (context.bindService(intent, advertisingConnection, 1)) {
                    try {
                        val adInterface =
                            AdvertisingInterface(
                                advertisingConnection.binder
                            )
                        val adInfo =
                            AdInfo(
                                adInterface.id,
                                adInterface.isLimitAdTrackingEnabled()
                            )
                        context.unbindService(advertisingConnection)
                        adInfo
                    } catch (e: Exception) {
                        println("Get IAdvertisingIdService exception $e")
                        //throw e
                        null
                    } catch (th: Throwable) {
                        context.unbindService(advertisingConnection)
                       // throw th
                        null
                    }
                } else null//throw IOException("Google Play connection failed")
            } catch (e2: Exception) {
                println("Cannot get package info com.android.vending $e2")
               // throw e2
                null
            }
        } else {
            println("AdvertisingIdClient Cannot be called from the main thread")
           //throw IllegalStateException("Cannot be called from the main thread")
            null
        }
    }

    const val ADVERTISING_ID_SERVICE_INTERFACE_TOKEN =
        "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService"

    class AdInfo internal constructor(val id: String, val isLimitAdTrackingEnabled: Boolean)

    private class AdvertisingConnection() : ServiceConnection {
        private val queue: LinkedBlockingQueue<IBinder> = LinkedBlockingQueue(1)
        var retrieved = false
        override fun onServiceDisconnected(componentName: ComponentName) {}
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                queue.put(iBinder)
            } catch (unused: InterruptedException) {
            }
        }

        /* access modifiers changed from: package-private */
        @get:Throws(InterruptedException::class)
        val binder: IBinder
            get() {
                if (!retrieved) {
                    retrieved = true
                    return queue.take()
                }
                throw IllegalStateException()
            }

    }

    private class AdvertisingInterface constructor(private val binder: IBinder) :
        IInterface {
        override fun asBinder(): IBinder {
            return binder
        }

        @get:Throws(RemoteException::class)
        val id: String
            get() {
                val obtain = Parcel.obtain()
                val obtain2 = Parcel.obtain()
                return try {
                    obtain.writeInterfaceToken(ADVERTISING_ID_SERVICE_INTERFACE_TOKEN)
                    binder.transact(1, obtain, obtain2, 0)
                    obtain2.readException()
                    obtain2.readString() ?: ""
                } finally {
                    obtain2.recycle()
                    obtain.recycle()
                }
            }

        /* access modifiers changed from: package-private */
        @Throws(RemoteException::class)
        fun isLimitAdTrackingEnabled(): Boolean {
            val obtain = Parcel.obtain()
            val obtain2 = Parcel.obtain()
            return try {
                obtain.writeInterfaceToken(ADVERTISING_ID_SERVICE_INTERFACE_TOKEN)
                var z2 = true
                obtain.writeInt(1)
                binder.transact(2, obtain, obtain2, 0)
                obtain2.readException()
                if (obtain2.readInt() == 0) z2 = false
                z2
            } finally {
                obtain2.recycle()
                obtain.recycle()
            }
        }
    }
}