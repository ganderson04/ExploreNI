package com.ganderson.exploreni.fragments


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.FINE_LOCATION_PERMISSION
import com.ganderson.exploreni.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.api.GOOGLE_API_KEY
import com.ganderson.exploreni.api.services.GeocodingService
import com.ganderson.exploreni.api.OPENWEATHERMAP_API_KEY
import com.ganderson.exploreni.api.services.WeatherService
import com.ganderson.exploreni.models.api.WeatherResponse
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    // "by lazy" initialises a variable when it is first required, not at runtime.
    private val weatherService by lazy { setupWeatherService() }
    private val geocodingService by lazy { setupGeocodingService() }

    private var locationManager: LocationManager? = null
    private var location: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Home"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)

        // Declares that this fragment will set its own menu.
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (activity as MainActivity).getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager
            getLocation()
            loadWeather()
        }
        else requestLocationPermission()
    }

    private fun setupWeatherService() : WeatherService {
        return Retrofit.Builder()
            .baseUrl(WeatherService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    private fun setupGeocodingService() : GeocodingService {
        val gson = GsonBuilder()
            .registerTypeAdapter(String::class.java, GeocodingService.GeocodingDeserialiser())
            .create()

        return Retrofit.Builder()
            .baseUrl(GeocodingService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GeocodingService::class.java)
    }

    private fun getLocation() {
        if(ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // If the LocationManager has been instantiated, check for providers. Kotlin "?"
            // performs a null check and ".let" runs the code inside the block if the object
            // under consideration is not null.
            locationManager?.let{
                // "it" refers to locationManager. "let" blocks are similar to lambdas.
                if(it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else if(it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                setLocationName()
//                tvWeatherTown.text = setLocationName()
            }
        }
    }

    private fun setLocationName() {
        // Attempt to get name via geolocation service.
        val geocodingData = HashMap<String, String>()
        geocodingData["latlng"] = location!!.latitude.toString() + "," + location!!.longitude.toString()
        geocodingData["result_type"] = GeocodingService.RESULT_TYPE
        geocodingData["key"] = GOOGLE_API_KEY

        val geocodingCall = geocodingService.reverseGeocode(geocodingData)
        geocodingCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                response.body()?.let {
                    tvWeatherTown.text = response.body().toString()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // Attempt to use Geocoder. It may sometimes return null.
                val geocoder: Geocoder? = Geocoder(activity, Locale.getDefault())

                if(geocoder != null) {
                    val addressList = geocoder
                        .getFromLocation(location!!.latitude, location!!.longitude, 1)
                    val address = addressList[0]
                    tvWeatherTown.text = address.subAdminArea
                }
                else {
                    tvWeatherTown.text = "Northern Ireland"
                    Toast.makeText(context, "Cannot retrieve location",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadWeather() {
        if(location != null) {
            // Assemble parameters for OpenWeatherMap API call.
            val weatherData = HashMap<String, String>()
            weatherData["lat"] = location!!.latitude.toString()
            weatherData["lon"] = location!!.longitude.toString()
            weatherData["units"] = "metric" //TODO: Get user measurement preference. Using metric.
            weatherData["APPID"] = OPENWEATHERMAP_API_KEY

            // Make, enqueue and process the call.
            val weatherCall = weatherService.getCurrentWeather(weatherData)
            weatherCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>,
                                        response: Response<WeatherResponse>) {
                    response.body()?.let {
                        tvWeatherDescription.text = it.weather[0].main
                        tvWeatherTemp.text = it.main.temp
                            .toInt() // Truncate decimal portion of temperature
                            .toString() + "°C" //TODO: Change between Fahrenheit/Celsius.
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(activity, "Weather load failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            tvWeatherTown.text = "Unknown"
            tvWeatherDescription.text = "Enable location to see weather."
            tvWeatherTemp.text = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.tb_emergency -> {
                Toast.makeText(this.activity!!, "Emergency selected",
                    Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.tb_settings -> {
                Toast.makeText(
                    this.activity!!, "Settings selected",
                    Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestLocationPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(context!!)
                .setTitle("Location requested")
                .setMessage("Your location is requested to show nearby attractions and " +
                        "weather updates.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION)
                }
                // Kotlin allows unusued lambda parameters to be named as "_".
                .setNegativeButton("Cancel") { dialog, _ -> dialog?.dismiss() }
                .create()
                .show()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder(context!!)
                .setTitle("Caution")
                .setMessage("Location denied. Some features of this app may not work properly.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog?.dismiss() }
                .show()
        }
        else {
            locationManager = (activity as MainActivity).getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager
            getLocation()
            loadWeather()
        }
    }
}
