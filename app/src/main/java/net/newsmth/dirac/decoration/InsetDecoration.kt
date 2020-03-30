package net.newsmth.dirac.decoration

import android.app.Activity
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class InsetDecoration(val act: Activity, val actionBarHeight: Int) : RecyclerView.ItemDecoration() {

    var ratio = 1f
    var topInset = actionBarHeight
    var bottomInset = 0

    fun applyInset(top: Int, bottom: Int) {
        topInset = actionBarHeight + top
        bottomInset = bottom
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        val t: Int
        val b: Int
        val pos = parent.getChildAdapterPosition(view)
        t = if (pos == 0) {
            topInset
        } else {
            0
        }
        b = if (pos + 1 == parent.adapter?.itemCount) {
            bottomInset
        } else {
            0
        }
        outRect.set(0, t, 0, b)
    }
}