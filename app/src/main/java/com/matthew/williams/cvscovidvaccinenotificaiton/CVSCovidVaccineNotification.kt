package com.matthew.williams.cvscovidvaccinenotificaiton

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log

class CVSCovidVaccineNotification : Application() {
    companion object {
        const val GENERAL_NOTIFICATION_CHANNEL = "GeneralNotificationChannel"
    }

    override fun onCreate() {
        super.onCreate()
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(GENERAL_NOTIFICATION_CHANNEL, "General", importance).apply {
                    description = "All notifications for this App"
                }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}