package me.elvis.indexablerecyclerview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils

class LayoutManagerActivity : AppCompatActivity() {
    private val mRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recycler_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_manager)

        mRecyclerView.layoutManager = StickyVerticalLayoutManager(this@LayoutManagerActivity)
    }
}


class StickyVerticalLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
    }
}