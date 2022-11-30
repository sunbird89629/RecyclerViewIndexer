package me.elvis.indexablerecyclerview

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    companion object {
        private const val items = """
            Creating a full-fledged REST(ful) API is a huge investment
             One can quickly test a simple API by exposing one’s database
             in a CRUD API via PostgREST However
             such an architecture is not fit for production usage
             To fix it you need to set a façade in front of PostgREST
             a reverse proxy or even better an API Gateway Apache APISIX
             offers a wide range of features from authorization to monitoring
             With it you can quickly validate your API requirements at a low cost
             The icing on the cake: when you’ve validated the requirements
             you can keep the existing façade and replace PostgREST with your custom-developed API
             The complete source code for this post can be found on Github
             Kotlin Android Extensions is deprecated which means that using Kotlin
             synthetics for view binding is no longer supported If your app uses
             Kotlin synthetics for view binding use this guide to migrate to Jetpack
             view binding If your app does not already use Kotlin synthetics
             for view binding see View binding for basic usage information
        """
        private val datas =
            items.split(" ")
                .asSequence()
                .map { it.trim() }
                .map { it.replace("\n", "") }
                .map { it.replaceFirstChar { firstChar -> firstChar.uppercaseChar() } }
                .distinct()
                .filter { it.isNotBlank() && it.length > 1 }
                .sortedBy { it.first() }
                .toList()

        private val indexes = datas.map { it.first() }.distinct().toList()

        private val indexAndData: List<Item>
            get() {
                val items = mutableListOf<Item>()
                val map: Map<Char, List<String>> = datas.groupBy { it.first() }
                for (key in map.keys) {
                    items.add(Item(key.toString(), true))
                    for (item in map[key] ?: emptyList()) {
                        items.add(Item(item, false))
                    }
                }
                return items
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val adapter = MyAdapter()
        list.layoutManager = StickyVerticalLayoutManager(this)
        list.adapter = adapter
        list.addItemDecoration(
            SectionItemDecoration(
                this@MainActivity,
                object : SectionItemDecoration.Callback {
                    override fun isSectionItem(position: Int): Boolean {
                        return indexAndData[position].isIndex
                    }

                    override fun geSectionContent(position: Int): String {
                        for (i in position downTo 0) {
                            val item = indexAndData[i]
                            if (item.isIndex) {
                                return item.content
                            }
                        }
                        return ""
                    }
                })
        )
    }

    inner class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SectionIndexer {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ItemType.SECTION -> SectionViewHolder(
                    layoutInflater.inflate(
                        R.layout.section,
                        parent,
                        false
                    )
                )
                else -> ItemViewHolder(layoutInflater.inflate(R.layout.item, parent, false))
            }
        }

        override fun getItemCount(): Int {
            return indexAndData.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val model = indexAndData[position]
            when (holder) {
                is SectionViewHolder -> {
                    holder.model = model
                    holder.bind()
                }
                is ItemViewHolder -> {
                    holder.model = model
                    holder.bind()
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            val item = indexAndData[position]
            return if (item.isIndex) {
                ItemType.SECTION
            } else {
                ItemType.ITEM
            }
        }

        override fun getSections(): Array<String> {
            return indexes.map { it.toString() }.toTypedArray()
        }

        override fun getSectionForPosition(position: Int): Int {
            return 0
        }

        override fun getPositionForSection(sectionIndex: Int): Int {
            val section = indexes[sectionIndex].toString()
            return indexAndData.indexOfFirst {
                it.content == section
            }
        }
    }
}

class Item(val content: String, val isIndex: Boolean)

class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var model: Item
    private val tvTitle: TextView = itemView.findViewById(R.id.tv)
    fun bind() {
        tvTitle.text = model.content
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var model: Item
    private val tvTitle: TextView = itemView.findViewById(R.id.tv)
    fun bind() {
        tvTitle.text = model.content
    }
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@IntDef(ItemType.SECTION, ItemType.ITEM)
annotation class ItemType {
    companion object {
        const val SECTION = 1
        const val ITEM = 2
    }
}

class StickyVerticalLayoutManager(context: Context) : LinearLayoutManager(context) {

}

class SectionItemDecoration(
    private val context: Context,
    private val callback: Callback
) : RecyclerView.ItemDecoration() {
    private val mTitleHeight: Int
    private val mTitleLayout: View = LayoutInflater.from(context).inflate(R.layout.section, null)
    private val mTvTitle: TextView = mTitleLayout.findViewById(R.id.tv)

    init {
        mTitleLayout.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED)
        mTitleHeight = mTitleLayout.measuredHeight
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val layoutManager = parent.layoutManager ?: return
        val firstVisiblePosition = findFirstVisibleItemPosition(layoutManager)
        val firstVisibleView =
            parent.findViewHolderForAdapterPosition(firstVisiblePosition)?.itemView ?: return
        if (firstVisiblePosition <= -1 || firstVisiblePosition >= parent.adapter!!.itemCount - 1) return

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        var top = parent.paddingTop

        if (
            nextLineIsTitle(
                firstVisibleView,
                firstVisiblePosition,
                parent
            )
        ) {
            top = if (firstVisibleView.bottom > mTitleHeight) {
                parent.paddingTop
            } else {
                firstVisibleView.bottom - mTitleHeight + parent.paddingTop
            }
        }
        drawStickySection(c, top, firstVisiblePosition, left, right)
    }

    private fun findFirstVisibleItemPosition(layoutManager: RecyclerView.LayoutManager): Int {
        return when (layoutManager) {
            is LinearLayoutManager -> {
                layoutManager.findFirstVisibleItemPosition()
            }
            is GridLayoutManager -> {
                layoutManager.findFirstVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                layoutManager.findFirstVisibleItemPositions(null)[0]
            }
            else -> {
                throw RuntimeException("The LayoutManager type ${layoutManager::class.java.name} is not supported!")
            }
        }
    }

    private fun drawStickySection(canvas: Canvas, top: Int, position: Int, left: Int, right: Int) {
        mTitleLayout.layout(left, 0, right, mTitleHeight)
        mTvTitle.text = callback.geSectionContent(position)
        canvas.save()
        canvas.translate(0f, top.toFloat())
        mTitleLayout.draw(canvas)
        canvas.restore()
    }

    private fun nextLineIsTitle(
        currentView: View,
        currentPosition: Int,
        parent: RecyclerView
    ): Boolean {
        val adapter = parent.adapter ?: return false
        for (nextLinePosition in currentPosition + 1 until adapter.itemCount) {
            val nextItemView =
                parent.findViewHolderForAdapterPosition(nextLinePosition)?.itemView ?: return false
            if (nextItemView.bottom > currentView.bottom) {
                return callback.isSectionItem(nextLinePosition)
            }
        }
        return false
    }


    interface Callback {
        /**
         * is  the ViewHolder at position type section
         */
        fun isSectionItem(position: Int): Boolean

        /**
         *
         */
        fun geSectionContent(position: Int): String
    }
}


