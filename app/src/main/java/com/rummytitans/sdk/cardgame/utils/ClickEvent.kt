package com.rummytitans.sdk.cardgame.utils

import android.os.SystemClock

import java.util.concurrent.ConcurrentHashMap

object ClickEvent {
    const val BUTTON_CLICK = "myteam11_btn_click"

    private val mHashMap = ConcurrentHashMap<String, Long>()

    fun check(tag: String) = check(tag, MutipleClick.default?.interval())

    private fun check(tag: String, intervalMs: Int?): Boolean {
        var result = false
        val current = SystemClock.elapsedRealtime()
        val last = mHashMap[tag]
        if (last != null) {
            if (last != -1L && current - last >= intervalMs ?: 0) result = true
        } else result = true
        if (result) mHashMap[tag] = current
        return result
    }

    internal class MutipleClick private constructor() {
        private var mInterval: Int = 1000
        fun interval() = mInterval

        companion object {
            private var sInstance: MutipleClick? = null
            val default: MutipleClick?
                get() = if (sInstance == null)
                    MutipleClick().also { sInstance = it } else sInstance
        }
    }
}