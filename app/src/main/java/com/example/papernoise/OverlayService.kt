package com.example.papernoise

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.view.WindowManager

class OverlayService : Service() {
    private var view: NoiseView? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        startFg()
        val p = getSharedPreferences("p", MODE_PRIVATE)
        p.edit().putBoolean("on", true).apply()
        if (view == null) {
            val lp = WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            if (Build.VERSION.SDK_INT >= 28)
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            view = NoiseView(this)
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(view, lp)
        }
        view?.update(p.getInt("intensity", 30), p.getInt("grain", 20), p.getInt("warmth", 15))
        return START_STICKY
    }

    private fun startFg() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("ov", "オーバーレイ", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(
            this, 0, Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, "ov")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("PaperNoise表示中")
            .addAction(Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                "停止", stop).build())
            .build()
        if (Build.VERSION.SDK_INT >= 34)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1, n)
    }

    override fun onDestroy() {
        view?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
        view = null
        getSharedPreferences("p", MODE_PRIVATE).edit().putBoolean("on", false).apply()
        super.onDestroy()
    }

    companion object { const val ACTION_STOP = "stop" }
}
