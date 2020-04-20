package com.ganderson.exploreni.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.couchbase.lite.CouchbaseLite
import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.fragments.*
import kotlinx.android.synthetic.main.activity_main.*

const val FINE_LOCATION_PERMISSION = 1
const val CAMERA_PERMISSION = 2

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

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}
