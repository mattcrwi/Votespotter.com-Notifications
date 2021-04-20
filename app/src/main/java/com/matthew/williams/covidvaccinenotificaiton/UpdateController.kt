package com.matthew.williams.covidvaccinenotificaiton

import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*
import kotlin.math.*


class UpdateController constructor(private val applicationContext: Context) {
    companion object {

        @Suppress("JoinDeclarationAndAssignment")
        private val objectMapper: ObjectMapper
        private val retrofit: Retrofit
        private val vaccineSpotterApi: VaccineSpotterApi
        private const val SP_LL = "spll"

        private var myLocation = Location(40.345015974270886, -75.9623988117034)

        init {

            objectMapper = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(JavaTimeModule())
            }
            val httpClient = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.HEADERS
            httpClient.addInterceptor(logging)

            retrofit = Retrofit.Builder()
                .baseUrl("https://www.cvs.com/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(httpClient.build())
                .build()

            vaccineSpotterApi = retrofit.create(VaccineSpotterApi::class.java)
        }
    }

    init {
        getLatLonFromDisk()?.let { myLocation = it }
    }

    suspend fun doUpdate() {
        try {
            val response = vaccineSpotterApi.getData(STATE_ABREVIATION)
            Log.d(this::class.simpleName, "got response: $response")

            val hasListChanged: Boolean
            val oldList = loadCityList()

            var citiesWithAvailability = ""
            response.features.forEach {
                if (it.properties?.appointmentsAvailable == true && distanceInMiles(
                        myLocation.latitude,
                        myLocation.longitude,
                        it.geometry?.coordinates?.get(1) ?: 0.0,
                        it.geometry?.coordinates?.get(0) ?: 0.0
                    ) < 25
                ) {


                    citiesWithAvailability += (it.properties?.providerBrandName + " " + it.properties?.city + "; ")
                }
            }
            saveCityList(citiesWithAvailability)
            hasListChanged = oldList != citiesWithAvailability

            if (response.features.firstOrNull { it.properties?.appointmentsAvailable == true } != null
                && hasListChanged && citiesWithAvailability.isNotBlank()) {

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
        val json = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
            .getString("data", null)

        val result =
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

    private fun distanceInMiles(
        lat1p: Double, lon1p: Double,
        lat2p: Double, lon2p: Double
    ): Double {
        return haversine(lat1p, lon1p, lat2p, lon2p) * 0.6213712
    }

    private fun haversine(
        lat1p: Double, lon1p: Double,
        lat2p: Double, lon2p: Double
    ): Double {
        // distance between latitudes and longitudes
        var lat1 = lat1p
        var lat2 = lat2p
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2p - lon1p)

        // convert to radians
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        // apply formulae
        val a = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) *
                cos(lat1) *
                cos(lat2)
        val rad = 6371.0
        val c = 2 * asin(sqrt(a))
        return rad * c
    }

    fun setLatLon(lat: Double, lon: Double) {
        myLocation.latitude = lat
        myLocation.longitude = lon
        applicationContext.getSharedPreferences(SP_LL, MODE_PRIVATE).edit().apply {
            putString("location", objectMapper.writeValueAsString(Location(lat, lon)))
            apply()
        }
    }

    fun getLatLonFromDisk(): Location? {
        applicationContext.getSharedPreferences(SP_LL, MODE_PRIVATE).apply {
            return getString("location", null)?.let { objectMapper.readValue<Location?>(it) }
        }
    }
}