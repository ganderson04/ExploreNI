package com.ganderson.exploreni.ui.activities

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import com.couchbase.lite.CouchbaseLite
import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.fragments.*
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

const val FINE_LOCATION_PERMISSION = 1
const val CAMERA_PERMISSION = 2

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private var lastLocationCallback: LocationCallback? = null
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        setupLocationService()

        bnvNavigation.setOnNavigationItemSelectedListener { menuItem ->
            var selectedFragment: Fragment? = null
            when(menuItem.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_explore -> selectedFragment = ExploreFragment()
                R.id.nav_ar -> selectedFragment = ArModeFragment()
                R.id.nav_plan -> selectedFragment = PlanFragment()
                R.id.nav_favourites -> selectedFragment = FavouritesFragment()
            }

            if(selectedFragment != null) displayFragment(selectedFragment)

            // Returning true highlights the chosen item on the bottom navbar.
            return@setOnNavigationItemSelectedListener true
        }

        // Start with the home screen.
        displayFragment(HomeFragment())

        CouchbaseLite.init(this)
    }

    /**
     * Convenience method created to load fragments and avoid repeated code.
     */
    fun displayFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupLocationService() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            // Request location in 1 minute intervals.
            interval = 60000

            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        lastLocationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                location = locationResult?.lastLocation
            }
        }

        fusedLocationProvider.requestLocationUpdates(locationRequest, lastLocationCallback,
            Looper.getMainLooper())
    }

    fun registerLocationCallback(callback: LocationCallback) {
        fusedLocationProvider
            .requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    }

    fun deregisterLocationCallback(callback: LocationCallback) {
        fusedLocationProvider.removeLocationUpdates(callback)
    }

    fun getLastLocation(): Location? {
        return location
    }

    override fun onPause() {
        super.onPause()
        lastLocationCallback?.let { deregisterLocationCallback(it) }
    }

    override fun onResume() {
        super.onResume()
        lastLocationCallback?.let { registerLocationCallback(it) }
    }

    override fun onBackPressed() {
        // The Material Design guidelines for the BottomNavigationView's (BNV) behaviour once stated
        // that the phone's back button does not navigate between navigation bar views. This has
        // been removed but not replaced with a new guideline. Indeed, Google's own apps featuring
        // BNVs behave differently to one another. Some developers have gone with the behaviour that
        // a back press returns to the home screen and a further back press closes the app.
        // Ref: https://www.reddit.com/r/androiddev/comments/814utk/back_button_behavior_in_bottom_navigation/
        //
        // Below, if the BNV's current ID does not equal the "Home" ID, return to the "Home" screen,
        // otherwise close the app.
        if(bnvNavigation.selectedItemId != R.id.nav_home) {
            displayFragment(HomeFragment())
            bnvNavigation.selectedItemId = R.id.nav_home
        }
        else {
            this.finish()
        }
    }
}
