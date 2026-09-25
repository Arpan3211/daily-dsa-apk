package com.dailydsa

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content)
        findViewById<TextView>(R.id.btn_refresh).setOnClickListener { refresh(showToast = true) }
        RefreshWorker.schedule(this)
        render()
        refresh(showToast = false)
    }

    private fun refresh(showToast: Boolean) {
        Thread {
            val ok = Store.refresh(applicationContext)
            runOnUiThread {
                if (ok) render()
                if (showToast) {
                    Toast.makeText(this, if (ok) "Updated" else "Couldn't fetch, try again", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun render() {
        val problem = Store.load(this)
        content.removeAllViews()
        if (problem == null) {
            content.addView(text("Loading today's problem…", 16f))
            return
        }
        content.addView(text("${problem.id}. ${problem.title}", 22f, bold = true))
        content.addView(text(problem.difficulty, 14f, color = Store.difficultyColor(problem.difficulty), bold = true))
        content.addView(text("${problem.date}  ·  ${problem.topics.joinToString(", ")}", 12f, color = 0xFF888888.toInt()))
        problem.blocks.forEach { b ->
            val tv = text(b.text, if (b.isCode) 13f else 15f)
            if (b.isCode) {
                tv.typeface = Typeface.MONOSPACE
                tv.setBackgroundColor(0xFFF0F0F0.toInt())
                tv.setPadding(dp(10), dp(8), dp(10), dp(8))
            }
            content.addView(tv)
        }
        content.addView(text("Open on LeetCode  →", 16f, color = 0xFF1A73E8.toInt(), bold = true).apply {
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(problem.link)))
            }
        })
    }

    private fun text(s: String, size: Float, color: Int = 0xFF222222.toInt(), bold: Boolean = false) =
        TextView(this).apply {
            text = s
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            setTextIsSelectable(false)
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
