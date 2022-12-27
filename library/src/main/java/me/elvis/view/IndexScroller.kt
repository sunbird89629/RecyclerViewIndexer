package me.elvis.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import android.widget.SectionIndexer
import android.graphics.RectF
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toRect
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.math.floor

class IndexScroller internal constructor(
    context: Context,
    attrs: AttributeSet?,
    private val mRv: RecyclerView
) : AdapterDataObserver() {
    companion object {
        val TAG = IndexScroller::class.simpleName
        val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#20000000")
    }

    private var mIndexer: SectionIndexer? = null
    private var mSections: Array<String>? = null
    private var mIndexbarRect: RectF? = null
    private val mIndexPaint: Paint
    private var mBackgroundColor: Int

    private var mTouchedBackgroundColor: Int
    private val mBackgroundPaint: Paint by lazy {
        Paint()
    }
    private val mDensity: Float
    private val mScaledDensity: Float
    private var mSingle = 0f
    private var mHeight = 0f
    private var mWidth = 0f
    private val mGap: Float
    private var mBaseLineToTop = 0f
    private var mMaxSingleWidth = 0f
    private val mPadding: Float
    private var mListViewWidth = 0
    private var mListViewHeight = 0
    private var mCurrentSection = -1
    private var mIsIndexing = false

    /**
     * is current index touched
     */
    private var mIndexTouched = false

    init {
        mDensity = context.resources.displayMetrics.density
        mScaledDensity = context.resources.displayMetrics.scaledDensity
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IndexRecyclerView)
        val textColor = ta.getColor(R.styleable.IndexRecyclerView_textColor, Color.BLACK)
        mPadding = ta.getDimensionPixelSize(
            R.styleable.IndexRecyclerView_padding,
            (10 * mDensity + 0.5).toInt()
        ).toFloat()
        mGap = ta.getDimensionPixelSize(
            R.styleable.IndexRecyclerView_padding,
            (3 * mDensity + 0.5).toInt()
        ).toFloat()
        val defTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            12f, context.resources.displayMetrics
        ).toInt()
        val theTextSize =
            ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_textSize, defTextSize)

        val theColor =
            ta.getColor(R.styleable.IndexRecyclerView_backgroundColor, DEFAULT_BACKGROUND_COLOR)
        mBackgroundColor = ColorUtils.setAlphaComponent(theColor, 0x20)
        mTouchedBackgroundColor = ColorUtils.setAlphaComponent(theColor, 0x60)
        ta.recycle()

        mIndexPaint = Paint().apply {
            color = textColor
            isAntiAlias = true
            textSize = theTextSize.toFloat()
            textAlign = Paint.Align.CENTER
        }

    }

    fun draw(canvas: Canvas) {
        drawBackground(canvas)
        drawIndex(canvas)
    }

    private fun drawIndex(canvas: Canvas) {
        if (mSections != null && mSections!!.isNotEmpty()) {
            if (mCurrentSection >= 0) {
                val previewPaint = Paint()
                previewPaint.color = Color.BLACK
                previewPaint.alpha = 96
                previewPaint.isAntiAlias = true
                previewPaint.setShadowLayer(3f, 0f, 0f, Color.argb(64, 0, 0, 0))
                val previewTextPaint = Paint()
                previewTextPaint.color = Color.WHITE
                previewTextPaint.isAntiAlias = true
                previewTextPaint.textSize = 50 * mScaledDensity
                val previewTextWidth = previewTextPaint.measureText(mSections!![mCurrentSection])
                val previewSize = previewTextPaint.descent() - previewTextPaint.ascent()
                val previewRect = RectF(
                    (mListViewWidth - previewSize) / 2,
                    (mListViewHeight - previewSize) / 2,
                    (mListViewWidth - previewSize) / 2 + previewSize,
                    (mListViewHeight - previewSize) / 2 + previewSize
                )
                canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity, previewPaint)
                canvas.drawText(
                    mSections!![mCurrentSection],
                    previewRect.left + (previewSize - previewTextWidth) / 2 - 1,
                    previewRect.top - previewTextPaint.ascent() + 1,
                    previewTextPaint
                )
            }
            for (i in mSections!!.indices) {
                canvas.drawText(
                    mSections!![i], mIndexbarRect!!.left + mMaxSingleWidth / 2 + mPadding,
                    mIndexbarRect!!.top + i * (mSingle + mGap) + mBaseLineToTop, mIndexPaint
                )
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val rect = mIndexbarRect ?: return
        mBackgroundPaint.color = if (mIndexTouched) {
            mTouchedBackgroundColor
        } else {
            mBackgroundColor
        }
        canvas.drawRoundRect(rect, 20f, 20f, mBackgroundPaint)
    }

    fun drawDebug(canvas: Canvas) {
        val rect = mIndexbarRect ?: return
        canvas.clipRect(rect.toRect())
        canvas.drawColor(Color.RED)
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> { // If down event occurs inside index bar region, start indexing
                if (contains(ev.x, ev.y)) {
                    mIndexTouched = true
                    // It demonstrates that the motion event started from index bar
                    mIsIndexing = true
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.y)
                    scrollToPosition(mIndexer!!.getPositionForSection(mCurrentSection))
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> if (mIsIndexing) {
                // If this event moves inside index bar
                if (contains(ev.x, ev.y)) {
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.y)
                    scrollToPosition(mIndexer!!.getPositionForSection(mCurrentSection))
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                mIndexTouched = false
                if (mIsIndexing) {
                    mIsIndexing = false
                    mCurrentSection = -1
                    mRv.invalidate()
                }
            }
        }
        return false
    }

    fun contains(x: Float, y: Float): Boolean {
        // Determine if the point is in index bar region, which includes the right margin of the bar
        return x >= mIndexbarRect!!.left && y >= mIndexbarRect!!.top && y <= mIndexbarRect!!.bottom
    }

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mListViewWidth = w
        mListViewHeight = h
        initRect()
        Log.i(TAG, "onSizeChanged>>mIndexbarRect:${mIndexbarRect.toString()}")
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        if (adapter is SectionIndexer) {
            mIndexer = adapter
            mSections = mIndexer!!.sections as Array<String>
            adapter.registerAdapterDataObserver(this)
            val fm = mIndexPaint.fontMetrics
            mSingle = fm.bottom - fm.top + fm.leading
            mBaseLineToTop = mSingle - fm.bottom
            mHeight = mSingle * mSections!!.size + mGap * (mSections!!.size - 1)
            for (s in mSections!!) {
                val w = mIndexPaint.measureText(s)
                if (mMaxSingleWidth < w) {
                    mMaxSingleWidth = w
                }
            }
            mWidth = mMaxSingleWidth + mPadding * 2
            initRect()
        }
    }

    private fun initRect() {
        val margin = (mListViewHeight - mHeight) / 2
        mIndexbarRect = RectF(
            mListViewWidth - mWidth,
            margin,
            mListViewWidth.toFloat(),
            mListViewHeight - margin
        )
    }

    private fun scrollToPosition(position: Int) {
        (mRv.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(position, 0)
    }

    private fun getSectionByPoint(y: Float): Int {
        return if (mSections == null || mSections!!.isEmpty()) {
            0
        } else {
            floor(((y - mIndexbarRect!!.top) / (mSingle + mGap)).toDouble()).toInt()
        }
    }

    override fun onChanged() {
        mSections = mIndexer!!.sections as Array<String>
        mRv.invalidate()
    }
}