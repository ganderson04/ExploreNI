package com.ganderson.exploreni

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.fragments.*
import kotlinx.android.synthetic.main.activity_main.*

private const val FINE_LOCATION_PERMISSION = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        bnvNavigation.setOnNavigationItemSelectedListener { menuItem ->
            var selectedFragment: Fragment? = null
            when(menuItem.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_explore -> selectedFragment = ExploreFragment()
                R.id.nav_ar -> selectedFragment = LookAroundFragment()
                R.id.nav_plan -> selectedFragment = PlanFragment()
                R.id.nav_favourites -> selectedFragment = FavouritesFragment()
            }

            if(selectedFragment != null) displayFragment(selectedFragment)

            // Returning true highlights the chosen item on the bottom navbar.
            return@setOnNavigationItemSelectedListener true
        }

        // Start with the home screen.
        displayFragment(HomeFragment())
        checkLocationPermission()
    }

    fun displayFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flFragment, fragment)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
            AlertDialog.Builder(this)
                .setTitle("Caution")
                .setMessage("Location denied. Some features of this app may not work properly.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog?.dismiss() }
                .show()
        }
    }

    private fun checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setTitle("Location requested")
                .setMessage("Your location is requested to show nearby attractions and " +
                        "weather updates.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION)
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog?.dismiss() }
                .create()
                .show()
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION)
        }
    }

    private fun requestCameraPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setTitle("Camera access requested")
                .setMessage("Camera access is requested to enable the AR functionality.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION)
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog?.dismiss() }
                .create()
                .show()
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION)
        }
    }
}
