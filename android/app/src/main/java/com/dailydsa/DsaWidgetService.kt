package com.dailydsa

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class DsaWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)

    private class Factory(private val ctx: Context) : RemoteViewsFactory {
        private var blocks: List<Block> = emptyList()

        override fun onCreate() {}

        override fun onDataSetChanged() {
            blocks = Store.load(ctx)?.blocks ?: emptyList()
        }

        override fun onDestroy() {}

        override fun getCount() = blocks.size

        override fun getViewAt(position: Int): RemoteViews {
            val block = blocks[position]
            val layout = if (block.isCode) R.layout.item_code else R.layout.item_text
            return RemoteViews(ctx.packageName, layout).apply {
                setTextViewText(R.id.txt, block.text)
                // Tapping a paragraph opens the full problem in the app.
                setOnClickFillInIntent(R.id.txt, Intent())
            }
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 2
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = true
    }
}
