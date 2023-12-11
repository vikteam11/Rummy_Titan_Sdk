package com.rummytitans.sdk.cardgame.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class SpacesItemDecoration(val mSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = mSpace
        outRect.right = mSpace
        outRect.bottom = mSpace
        outRect.top = mSpace
    }
}

open class LinearSpaceItemDecoration(space: Int) : SpacesItemDecoration(space) {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {

        convertIntToDp(mSpace, view).let { dpMargin ->
            parent.getChildAdapterPosition(view).let {
                if (it == 0) {
                    outRect.top = dpMargin
                    outRect.bottom = dpMargin
                } else outRect.bottom = dpMargin
                outRect.left = dpMargin
                outRect.right = dpMargin
            }
        }
    }
}


open class CustomSpaceItemDecoration(
    space: Int, val body: (pos: Int, outRect: Rect, margin: Int) -> Unit
) : SpacesItemDecoration(space) {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        body(parent.getChildAdapterPosition(view), outRect, convertIntToDp(mSpace, view))
    }
}


class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        if (includeEdge) {
            outRect.left =
                spacing - column * spacing / spanCount // spacing - column  ((1f / spanCount) * spacing)
            outRect.right =
                (column + 1) * spacing / spanCount // (column + 1)  ((1f / spanCount) * spacing)
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount // column  ((1f / spanCount) * spacing)
            outRect.right =
                spacing - (column + 1) * spacing / spanCount // spacing - (column + 1)  ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }

}
