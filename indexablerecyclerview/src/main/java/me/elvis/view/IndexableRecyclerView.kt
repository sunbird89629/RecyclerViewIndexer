package me.elvis.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class IndexableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private val mIndexScroller: IndexScroller
    var isEnableIndex = true

    init {
        mIndexScroller = IndexScroller(context, attrs, this)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (isEnableIndex) {
            mIndexScroller.draw(canvas)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Intercept ListView's touch event
        return if (isEnableIndex && mIndexScroller.onTouchEvent(ev)) {
            true
        } else super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isEnableIndex && mIndexScroller.contains(
                ev.x,
                ev.y
            )
        ) true else super.onInterceptTouchEvent(
            ev
        )
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        mIndexScroller.setAdapter(adapter)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mIndexScroller.onSizeChanged(w, h, oldw, oldh)
    }
}