package com.rummytitans.playcashrummyonline.cardgame.widget

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

open class FontSpan(private val font: Typeface?) : MetricAffectingSpan() {

    override fun updateMeasureState(textPaint: TextPaint) = updateTypeface(textPaint)

    override fun updateDrawState(textPaint: TextPaint) = updateTypeface(textPaint)

    private fun updateTypeface(textPaint: TextPaint) {
        textPaint.apply {
            val oldStyle = getOldStyle(typeface)
            if (oldStyle == 0) return
            typeface = Typeface.create(font, oldStyle)
        }
    }

    private fun getOldStyle(typeface: Typeface?) = typeface?.style ?: 0
}