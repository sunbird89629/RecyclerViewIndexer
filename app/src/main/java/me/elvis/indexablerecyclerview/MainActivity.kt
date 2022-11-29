package me.elvis.indexablerecyclerview

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val adapter = MyAdapter()
        list.layoutManager =
            LinearLayoutManager(this)
        list.adapter = adapter
    }

    inner class MyAdapter : RecyclerView.Adapter<ViewHolder>(), SectionIndexer {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item, p0, false))
        }

        override fun getItemCount(): Int {
            return indexAndData.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = indexAndData[position]
            holder.tv.text = item.content
            val backgroundColorResId = if (item.isIndex) {
                R.color.listSection
            } else {
                R.color.listItem
            }
            holder.tv.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    backgroundColorResId
                )
            )
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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tv)
    }


}

class Item(val content: String, val isIndex: Boolean)
