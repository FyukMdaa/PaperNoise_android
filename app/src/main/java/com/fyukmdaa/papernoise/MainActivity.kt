package com.fyukmdaa.papernoise

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var p: SharedPreferences
    private lateinit var btn: Button

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        p = getSharedPreferences("p", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 120, 48, 48)
        }
        btn = Button(this).also { it.setOnClickListener { toggle() } }
        root.addView(btn)
        fun slider(label: String, key: String, def: Int) {
            root.addView(TextView(this).apply { text = label; setPadding(0, 48, 0, 0) })
            root.addView(SeekBar(this).apply {
                max = 100
                progress = p.getInt(key, def)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) {
                        p.edit().putInt(key, v).apply()
                        if (p.getBoolean("on", false)) start()
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            })
        }
        slider("ノイズの濃さ", "intensity", 30)
        slider("粒の大きさ", "grain", 20)
        slider("暖色度合い", "warmth", 15)
        setContentView(root)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        btn.text = if (p.getBoolean("on", false)) "オーバーレイを停止" else "オーバーレイを開始"
    }

    private fun start() = startForegroundService(Intent(this, OverlayService::class.java))

    private fun toggle() {
        if (p.getBoolean("on", false)) {
            startService(Intent(this, OverlayService::class.java).setAction(OverlayService.ACTION_STOP))
            p.edit().putBoolean("on", false).apply()
        } else {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
                return
            }
            if (Build.VERSION.SDK_INT >= 33)
                requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
            start()
            p.edit().putBoolean("on", true).apply()
        }
        refresh()
    }
}
