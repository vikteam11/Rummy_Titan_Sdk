package com.rummytitans.sdk.cardgame.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundedImageView : AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :
            super(context, attrs, defStyle)

    private val radius = 12.0f
    private var path: Path = Path()
    private var rect: RectF? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        rect = RectF(0f, 0f, this.width.toFloat(), this.height.toFloat())
        path.addRoundRect(rect!!, radius, radius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}