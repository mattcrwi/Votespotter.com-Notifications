package com.matthew.williams.covidvaccinenotificaiton

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
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
        private var updateController: UpdateController? = null
    }


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        updateController = UpdateController(applicationContext)


        updateController?.getLatLonFromDisk()?.let {
            binding.inputLatitude.setText(it.latitude.toString())
            binding.inputLongitude.setText(it.longitude.toString())
        }
    }


    override fun onResume() {
        super.onResume()

        binding.inputLatitude.doOnTextChanged { text, start, before, count ->
            if (text?.length ?: 0 > 0) {
                try {
                    val lat = text.toString().toDouble()
                    val lon = binding.inputLongitude.text.toString().toDouble()
                    updateController?.setLatLon(lat, lon)
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, "parse error probs", e)
                }
            }
        }


        binding.inputLongitude.doOnTextChanged { text, start, before, count ->
            if (text?.length ?: 0 > 0) {
                try {
                    val lon = text.toString().toDouble()
                    val lat = binding.inputLatitude.text.toString().toDouble()
                    updateController?.setLatLon(lat, lon)
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, "parse error probs", e)
                }
            }
        }

        CoroutineScope(IO).launch {
            updateController?.doUpdate()
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
