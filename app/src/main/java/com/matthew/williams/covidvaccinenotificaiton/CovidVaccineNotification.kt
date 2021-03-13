package com.matthew.williams.covidvaccinenotificaiton

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

class CovidVaccineNotification : Application() {
    companion object {
        const val GENERAL_NOTIFICATION_CHANNEL = "GeneralNotificationChannel"
        const val STATE_ABREVIATION = "PA"
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


        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(1, ComponentName(this, DownloadService::class.java))
        val job = jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(60000, 30000)
            .build()

        jobScheduler.schedule(job)
    }
}