package com.dailydsa

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Block(val isCode: Boolean, val text: String)

data class Problem(
    val date: String,
    val id: String,
    val title: String,
    val difficulty: String,
    val link: String,
    val topics: List<String>,
    val blocks: List<Block>,
)

/** Downloads daily.json, caches the raw JSON in SharedPreferences and parses it. */
object Store {
    private const val PREFS = "daily_dsa"
    private const val KEY_JSON = "json"

    fun load(ctx: Context): Problem? {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_JSON, null)
            ?: return null
        return runCatching { parse(raw) }.getOrNull()
    }

    /** Blocking network call; run off the main thread. Updates widgets on success. */
    fun refresh(ctx: Context): Boolean {
        return try {
            val conn = URL(BuildConfig.DATA_URL + "?t=" + System.currentTimeMillis())
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            val raw = conn.inputStream.bufferedReader().use { it.readText() }
            parse(raw) // validate before caching
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_JSON, raw).apply()
            DsaWidgetProvider.updateAll(ctx)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parse(raw: String): Problem {
        val o = JSONObject(raw)
        val blocks = o.getJSONArray("blocks")
        val topics = o.getJSONArray("topics")
        return Problem(
            date = o.getString("date"),
            id = o.getString("id"),
            title = o.getString("title"),
            difficulty = o.getString("difficulty"),
            link = o.getString("link"),
            topics = List(topics.length()) { topics.getString(it) },
            blocks = List(blocks.length()) {
                val b = blocks.getJSONObject(it)
                Block(b.getString("t") == "code", b.getString("v"))
            },
        )
    }

    fun difficultyColor(difficulty: String): Int = when (difficulty) {
        "Easy" -> 0xFF00B8A3.toInt()
        "Medium" -> 0xFFFFC01E.toInt()
        else -> 0xFFFF375F.toInt()
    }
}
