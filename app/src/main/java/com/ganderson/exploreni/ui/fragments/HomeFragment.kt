package com.ganderson.exploreni.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.ganderson.exploreni.ui.activities.FINE_LOCATION_PERMISSION
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.viewmodels.HomeViewModel
import com.ganderson.exploreni.ui.activities.SettingsActivity
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {
    private val viewModel = HomeViewModel()

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var useFahrenheit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationService()
        }
        else requestLocationPermission()

        cvEvents.setOnClickListener {
            val eventFragment = EventFragment()
            val mainActivity = this.activity as MainActivity
            mainActivity.displayFragment(eventFragment)
        }
    }

    private fun setLocationName(locationResult: LocationResult?) {
        if(locationResult != null) {
            val location = locationResult.lastLocation
            try {
                viewModel.getLocationName(
                    location.latitude,
                    location.longitude,
                    resources.getString(R.string.google_api_key)
                ).observe(viewLifecycleOwner) { name ->
                    tvWeatherTown.text = name
                }
            }
            catch(e: Exception) {
                // Attempt to use Geocoder. It may sometimes return null.
                val geocoder: Geocoder? = Geocoder(activity, Locale.getDefault())

                if(geocoder != null) {
                    val addressList = geocoder
                        .getFromLocation(location!!.latitude, location.longitude, 1)
                    val address = addressList[0]
                    tvWeatherTown.text = address.subAdminArea
                }
                else {
                    tvWeatherTown.text = "Northern Ireland"
                    Toast.makeText(context, "Cannot retrieve location",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            tvWeatherTown.text = "Northern Ireland"
            Toast.makeText(context, "Cannot retrieve location",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun getWeather(locationResult: LocationResult?) {
        if(locationResult != null) {
            val location = locationResult.lastLocation
            viewModel.getWeather(location.latitude,
                location.longitude,
                useFahrenheit,
                resources.getString(R.string.openweathermap_api_key)
            ).observe(viewLifecycleOwner) {
                var symbol = "°C"
                if(useFahrenheit) symbol = "°F"

                tvWeatherDescription.text = it.desc

                // Decimal portion of temperature truncated with toInt().
                val tempText = "${it.temp.toInt()}$symbol"
                tvWeatherTemp.text = tempText
            }
        }
        else {
            tvWeatherTown.text = "Unknown"
            tvWeatherDescription.text = "Enable location to see weather."
            tvWeatherTemp.text = ""
        }
    }

    private fun showEmergencyDialog() {
        val emergencyLayout = layoutInflater.inflate(R.layout.dialog_emergency, null)
        val number = emergencyLayout.findViewById<LinearLayout>(R.id.llNumber)
        number.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:999")
            startActivity(callIntent)
        }

        AlertDialog.Builder(requireContext())
            .setView(emergencyLayout)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.tb_emergency -> {
                showEmergencyDialog()
                return true
            }

            R.id.tb_settings -> {
                openSettings()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestLocationPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireContext())
                .setTitle("Location requested")
                .setMessage("Your location is requested to show nearby attractions and " +
                        "weather updates.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION
                    )
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
            AlertDialog.Builder(requireContext())
                .setTitle("Caution")
                .setMessage("Location denied. Some features of this app may not work properly.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog?.dismiss() }
                .show()
        }
        else {
            setupLocationService()
        }
    }

    private fun setupLocationService() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            // Request location in 1 minute intervals.
            interval = 60000

            // No need for high accuracy on this screen as we only wish to get the
            // town name and weather.
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                setLocationName(locationResult)
                getWeather(locationResult)

                cvNearby.setOnClickListener {
                    if (locationResult != null) {
                        val nearbyFragment = NearbyFragment(locationResult.lastLocation)
                        val mainActivity = this@HomeFragment.requireActivity() as MainActivity
                        mainActivity.displayFragment(nearbyFragment)
                    }
                    else {
                        Toast.makeText(requireContext(), "Not available with location disabled.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback,
            Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        // Remove request for location updates as the fragment is being hidden or destroyed.
        locationCallback?.let {
            fusedLocationProvider.removeLocationUpdates(it)
        }
    }

    override fun onResume() {
        // Check for settings change for temperature units
        if (PreferenceManager.getDefaultSharedPreferences(this.context)
            .getBoolean("measurement_temperature", false) != useFahrenheit) {
            useFahrenheit = !useFahrenheit

            // Reinstate request for location updates as the fragment is being shown again or
            // (re)created.
            locationRequest?.let {
                setupLocationService()
            }
        }

        super.onResume()
    }
}
