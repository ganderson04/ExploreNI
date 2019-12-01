package com.ganderson.exploreni.fragments


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.api.OPENWEATHERMAP_API_KEY
import com.ganderson.exploreni.api.WeatherService
import com.ganderson.exploreni.models.api.WeatherResponse
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var retrofit: Retrofit
    private lateinit var weatherService: WeatherService

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

        setHasOptionsMenu(true)

        retrofit = Retrofit.Builder()
            .baseUrl(WeatherService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWeather()
    }

    private fun loadWeather() {
        // Assemble parameters for OpenWeatherMap API call.
        val weatherData = HashMap<String, String>()
        weatherData["lat"] = "54.596675" //TODO: Get user location. Using Belfast city centre.
        weatherData["lon"] = "-5.930073"
        weatherData["units"] = "metric" //TODO: Get user measurement preference. Using metric.
        weatherData["APPID"] = OPENWEATHERMAP_API_KEY

        // Make, enqueue and process the call.
        val weatherCall = weatherService.getCurrentWeather(weatherData)
        weatherCall.enqueue(object: Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>,
                                    response: Response<WeatherResponse>) {
                val weatherResponse = response.body()!! // Assert non-null
                tvWeatherDescription.text = weatherResponse.weather[0].main
                tvWeatherTemp.text = weatherResponse
                    .main
                    .temp.toInt() // Truncate decimal portion of temperature
                    .toString() + "°C" //TODO: Change between Fahrenheit/Celsius according to prefs.
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(activity, "Weather load failed", Toast.LENGTH_SHORT).show()
            }
        })
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


}
