package com.example.plusoverlay

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val onBtn = Button(this).apply {
            text = "Yoqish"
            setOnClickListener { startOverlay() }
        }
        val offBtn = Button(this).apply {
            text = "O'chirish"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
            }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(onBtn)
            addView(offBtn)
        })
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Ruxsatni yoqing va qayta Yoqish ni bosing", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
    }
}

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var plusView: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        showPlus()
    }

    private fun startAsForeground() {
        val channelId = "plus_overlay"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Plus belgisi", NotificationManager.IMPORTANCE_LOW)
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Plus belgisi yoniq")
            .setContentText("O'chirish uchun ilovaga kirib O'chirish ni bosing")
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this, 1, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1, notification)
        }
    }

    private fun showPlus() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        plusView = TextView(this).apply {
            text = "+"
            setTextColor(Color.RED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            gravity = Gravity.CENTER
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager?.addView(plusView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        plusView?.let { windowManager?.removeView(it) }
        plusView = null
        super.onDestroy()
    }
}
