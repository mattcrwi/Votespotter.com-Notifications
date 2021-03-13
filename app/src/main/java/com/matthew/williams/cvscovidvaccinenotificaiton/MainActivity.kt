package com.matthew.williams.cvscovidvaccinenotificaiton

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.matthew.williams.cvscovidvaccinenotificaiton.CVSCovidVaccineNotification.Companion.GENERAL_NOTIFICATION_CHANNEL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import java.lang.Exception
import java.time.LocalDateTime


class MainActivity : AppCompatActivity() {
    companion object {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(1, ComponentName(this, CvsDownloadService::class.java))
        val job = jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(60000, 30000)
            .build()

        jobScheduler.schedule(job)

        CoroutineScope(IO).launch {
            UpdateService(applicationContext).doUpdate()
        }
        //"immunizations/covid-19-vaccine/immunizations/covid-19-vaccine.vaccine-status.PA.json?vaccineinfo?"
    }
}


interface CvsService {
    @GET("immunizations/covid-19-vaccine/immunizations/covid-19-vaccine.vaccine-status.PA.json?vaccineinfo")
    suspend fun getData(): CvsResponse
}

data class CvsResponse constructor(
    var responsePayloadData: CvsResponsePayloadData? = null
) {
}

data class CvsResponsePayloadData constructor(
    var currentTime: LocalDateTime?,
    var data: HashMap<String, Array<CvsStateData>>
) {

}

data class CvsStateData constructor(
    var city: String?,
    var state: String?,
    var status: String?
)