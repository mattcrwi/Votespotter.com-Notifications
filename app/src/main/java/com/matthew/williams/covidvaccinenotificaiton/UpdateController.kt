package com.matthew.williams.covidvaccinenotificaiton

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
import com.matthew.williams.covidvaccinenotificaiton.CovidVaccineNotification.Companion.STATE_ABREVIATION
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*

class UpdateController constructor(private val applicationContext: Context) {
    companion object {

        @Suppress("JoinDeclarationAndAssignment")
        private val objectMapper: ObjectMapper
        private val retrofit: Retrofit
        private val vaccineSpotterApi: VaccineSpotterApi

        init {

            objectMapper = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(JavaTimeModule())
            }

            retrofit = Retrofit.Builder()
                .baseUrl("https://www.cvs.com/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()

            vaccineSpotterApi = retrofit.create(VaccineSpotterApi::class.java)
        }
    }

    suspend fun doUpdate() {
        try {
            val response = vaccineSpotterApi.getData(STATE_ABREVIATION)
            Log.d(this::class.simpleName, "got response: $response")

            val hasListChanged: Boolean
            val oldList = loadCityList()

            var citiesWithAvailability: String = ""
            response.features.forEach {
                if (it.properties?.appointmentsAvailable == true && it.properties?.providerBrandName != "Rite Aid") {
                    citiesWithAvailability += (it.properties?.providerBrandName + " " + it.properties?.city + "; ")
                }
            }
            saveCityList(citiesWithAvailability)
            hasListChanged = oldList != citiesWithAvailability

            if (response.features.firstOrNull {
                    it.properties?.appointmentsAvailable == true
                            && it.properties?.providerBrandName != "Rite Aid"
                } != null
                && hasListChanged) {

                popNotification(citiesWithAvailability)
            }


        } catch (e: Exception) {
            Log.e(this::class.simpleName, "failed update", e)
        }
    }

    private fun popNotification(messageText: String?) {

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(
            applicationContext,
            CovidVaccineNotification.GENERAL_NOTIFICATION_CHANNEL
        )
            .setContentTitle("Covid Vaccine Available")
            .setContentText(messageText)
            .setSmallIcon(R.drawable.syringe)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(
                UUID.randomUUID().toString(),
                1,
                builder.build()
            )
        }
    }

    private fun saveData(data: FeatureCollection?) {
        applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE).edit().run {
            putString("data", objectMapper.writeValueAsString(data))
            commit()
        }
    }

    private fun loadData(): FeatureCollection? {
        var json = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
            .getString("data", null)

        var result =
            if (json == null) null
            else objectMapper.readValue<FeatureCollection>(json)

        return result
    }

    private fun saveCityList(data: String?) {
        applicationContext.getSharedPreferences("cities", Context.MODE_PRIVATE).edit().run {
            putString("data", data)
            commit()
        }
    }

    fun loadCityList(): String? {
        return applicationContext.getSharedPreferences("cities", Context.MODE_PRIVATE)
            .getString("data", null)
    }
}