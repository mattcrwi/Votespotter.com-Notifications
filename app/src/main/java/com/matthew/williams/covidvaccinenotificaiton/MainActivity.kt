package com.matthew.williams.covidvaccinenotificaiton

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matthew.williams.covidvaccinenotificaiton.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path


class MainActivity : AppCompatActivity() {
    companion object {
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(IO).launch {
            UpdateController(applicationContext).doUpdate()

            withContext(Main) {
                binding.tvBody.text = UpdateController(applicationContext).loadCityList()
            }
        }
    }
}

interface VaccineSpotterApi {
    @GET("https://www.vaccinespotter.org/api/v0/states/{state}.json")
    suspend fun getData(@Path("state") state: String): FeatureCollection
}
