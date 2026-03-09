package com.example.myapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RotationListenerService : Service() {

    private lateinit var sensorUtils: SensorUtils

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        sensorUtils = SensorUtils(
            context = this,

            onRotateDetected = {
                NotificationUtil.showRotationNotification(this)
            },
            onSensorValuesChange = { _, _, _ -> }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground
        startForeground(FOREGROUND_SERVICE_ID, createForegroundNotification())
        // Start listening for sensor events
        sensorUtils.start()
        // Ensures the service is restarted if killed by the system
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorUtils.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createForegroundNotification(): Notification {
        val channelId = "rotation_listener_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rotation Listener",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Listening for Device Rotation")
            .setContentText("The app is running in the background to detect rotation.")
            .setSmallIcon(R.drawable.baseline_catching_pokemon_24)
            .build()
    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 1
    }
}