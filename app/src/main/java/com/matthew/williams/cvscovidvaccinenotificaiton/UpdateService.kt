package com.matthew.williams.cvscovidvaccinenotificaiton

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class UpdateService constructor(private val applicationContext: Context) {
    companion object {

        @Suppress("JoinDeclarationAndAssignment")
        private val objectMapper: ObjectMapper
        private val retrofit: Retrofit
        private val cvsService: CvsService

        init {

            objectMapper = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(JavaTimeModule())
            }

            retrofit = Retrofit.Builder()
                .baseUrl("https://www.cvs.com/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()

            cvsService = retrofit.create(CvsService::class.java)
        }
    }

    suspend fun doUpdate() {
        try {
            val response = cvsService.getData()
            Log.d(this::class.simpleName, "got response: $response")

            var hasListChanged = false
            var oldList = loadsList()
            saveList(response.responsePayloadData?.data)

            hasListChanged = oldList != response.responsePayloadData?.data

            if (response.responsePayloadData?.data?.get("PA")
                    ?.firstOrNull { it.status != "Fully Booked" } == null || !hasListChanged
            ) return

            var citiesWithAvailability: String = ""
            response.responsePayloadData?.data?.get("PA")
                ?.forEach {
                    if (it.status != "Fully Booked") {
                        citiesWithAvailability += (it.city + ", ")
                    }
                }

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(applicationContext, 0, intent, 0)

            val builder = NotificationCompat.Builder(
                applicationContext,
                CVSCovidVaccineNotification.GENERAL_NOTIFICATION_CHANNEL
            )
                .setContentTitle("CVS Covid Vaccine Available")
                .setContentText(citiesWithAvailability)
                .setSmallIcon(R.drawable.syringe)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(citiesWithAvailability)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(applicationContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(
                    response.responsePayloadData?.currentTime.toString() ?: "No Data",
                    1,
                    builder.build()
                )
            }


        } catch (e: Exception) {
            Log.e(this::class.simpleName, "failed update", e)
        }
    }

    private fun saveList(data: HashMap<String, Array<CvsStateData>>?) {
        applicationContext.getSharedPreferences("CVS", Context.MODE_PRIVATE).edit().run {
            putString("data", objectMapper.writeValueAsString(data))
            commit()
        }
    }

    private fun loadsList(): HashMap<String, Array<CvsStateData>>? {
        var json = applicationContext.getSharedPreferences("CVS", Context.MODE_PRIVATE)
            .getString("data", null)

        var result =
            if (json == null) null
            else objectMapper.readValue<HashMap<String, Array<CvsStateData>>>(json)

        return result
    }
}