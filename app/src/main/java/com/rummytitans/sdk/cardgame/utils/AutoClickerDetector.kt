package com.rummytitans.sdk.cardgame.utils

import android.util.Log
import android.view.MotionEvent

class AutoClickerDetector {

    private var gestureStart: Touch? = null
    private val recentTouches = ArrayList<Gesture>()

    var isDetectAutoClick = false
        private set

    private data class Touch(
        val x: Float,
        val y: Float
        ) {
    }

    private data class Gesture(
        val start: Touch,
        val end: Touch
    ) {
    }

    fun recordEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                gestureStart = Touch(event.rawX, event.rawY)
            }
            MotionEvent.ACTION_UP -> {
                gestureStart?.let { gestureStart ->
                    val gestureEnd = Touch(event.rawX, event.rawY)
                    recentTouches.add(Gesture(gestureStart, gestureEnd))
                    trimGestureHistory()
                    checkForAutoClick()
                }
                gestureStart = null
            }
        }
    }
    private val HISTORY_SIZE = 20

    private fun trimGestureHistory() {
        while (recentTouches.size > HISTORY_SIZE) {
            recentTouches.removeAt(0)
        }
    }

    /*if any 5 items which have same coordinates then this click done by AutoCLick apps*/
    private fun checkForAutoClick() {
        val map = recentTouches.groupBy {
            "${it.start.x}/${it.start.y}-${it.end.x}/${it.end.y}"
        }
        isDetectAutoClick = map.any {
            map[it.key]?.size?:0 >= 5
            // There is no chance that user can physically perform almost exactly the same gesture
            // 5 times amongst 20 last gestures
        }
        Log.d("checkSimilarGestures","autoClick "+isDetectAutoClick)
    }
}