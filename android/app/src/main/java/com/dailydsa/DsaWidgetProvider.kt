package com.dailydsa

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class DsaWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        RefreshWorker.schedule(context)
        RefreshWorker.refreshNow(context)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        RefreshWorker.schedule(context)
        ids.forEach { manager.updateAppWidget(it, buildViews(context, it)) }
        manager.notifyAppWidgetViewDataChanged(ids, R.id.list)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) RefreshWorker.refreshNow(context)
    }

    companion object {
        private const val ACTION_REFRESH = "com.dailydsa.REFRESH"

        fun updateAll(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = manager.getAppWidgetIds(ComponentName(ctx, DsaWidgetProvider::class.java))
            if (ids.isEmpty()) return
            ids.forEach { manager.updateAppWidget(it, buildViews(ctx, it)) }
            manager.notifyAppWidgetViewDataChanged(ids, R.id.list)
        }

        private fun buildViews(ctx: Context, widgetId: Int): RemoteViews {
            val views = RemoteViews(ctx.packageName, R.layout.widget_daily)
            val problem = Store.load(ctx)

            if (problem == null) {
                views.setTextViewText(R.id.title, "Daily DSA")
                views.setTextViewText(R.id.difficulty, "")
                views.setTextViewText(R.id.meta, "Loading today's problem…")
            } else {
                views.setTextViewText(R.id.title, "${problem.id}. ${problem.title}")
                views.setTextViewText(R.id.difficulty, problem.difficulty)
                views.setTextColor(R.id.difficulty, Store.difficultyColor(problem.difficulty))
                views.setTextViewText(
                    R.id.meta,
                    "  ·  ${problem.date}  ·  ${problem.topics.take(3).joinToString(", ")}"
                )
            }

            // Scrollable statement: a collection backed by DsaWidgetService.
            val serviceIntent = Intent(ctx, DsaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.list, serviceIntent)
            views.setEmptyView(R.id.list, R.id.empty)

            val openApp = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.header, openApp)
            views.setPendingIntentTemplate(R.id.list, openApp)

            val refresh = PendingIntent.getBroadcast(
                ctx, 1,
                Intent(ctx, DsaWidgetProvider::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refresh, refresh)
            return views
        }
    }
}
