package com.matthew.williams.cvscovidvaccinenotificaiton

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CvsDownloadService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(this::class.simpleName, "job run")

        CoroutineScope(Dispatchers.IO).launch {
            UpdateService(applicationContext).doUpdate()
            jobFinished(params, false)
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(this::class.simpleName, "job stopped")
        return true
    }

}