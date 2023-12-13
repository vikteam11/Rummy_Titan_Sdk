package com.rummytitans.playcashrummyonline.cardgame.widget

import com.rummytitans.playcashrummyonline.cardgame.utils.convertDpToPixel
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class FixedRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
            super(context, attrs, defStyle)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(
            widthSpec, MeasureSpec.makeMeasureSpec(
                convertDpToPixel(200f, this.context), MeasureSpec.AT_MOST
            )
        )
    }
}