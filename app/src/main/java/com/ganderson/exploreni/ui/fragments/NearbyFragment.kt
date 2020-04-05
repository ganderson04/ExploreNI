package com.ganderson.exploreni.ui.fragments

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.viewmodels.NearbyViewModel
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_nearby.*

/**
 * A simple [Fragment] subclass.
 */
class NearbyFragment(private val userLocation: Location) : Fragment() {
    private val viewModel: NearbyViewModel =
        NearbyViewModel()
    private var useMetric = false
    private var currentSeekRadius = 5
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Nearby"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Declares that this fragment will set its own menu.
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frgMap = childFragmentManager.findFragmentById(R.id.frgMap)
                as SupportMapFragment

        useMetric = PreferenceManager
            .getDefaultSharedPreferences(this.context)
            .getBoolean("measurement_distance", false)

        if(useMetric) {
            tvCurrentRange.text = "${currentSeekRadius}km"
            skbNearbyRange.max = Utils.MAX_SEEK_KM
            tvMaxRange.text = "${Utils.MAX_SEEK_KM}km"
        }
        else {
            tvCurrentRange.text = "${currentSeekRadius}mi"
            skbNearbyRange.max = Utils.MAX_SEEK_MILES
            tvMaxRange.text = "${Utils.MAX_SEEK_MILES}mi"
        }
        skbNearbyRange.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int,
                                           fromUser: Boolean) {
                seekBar?.let {
                    if(useMetric) {
                        tvCurrentRange.text = "${progress}km"
                    }
                    else {
                        tvCurrentRange.text = "${progress}mi"
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    if (it.progress != currentSeekRadius) {
                        currentSeekRadius = it.progress
                        getNearbyLocations()
                    }
                }
            }
        })

        frgMap.getMapAsync {
            this.map = it
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            map.setOnInfoWindowClickListener{ marker ->
                val attractionDetailFragment =
                    AttractionDetailFragment(marker.tag as NiLocation, true)

                (this.activity as MainActivity).displayFragment(attractionDetailFragment)
            }

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(userLocation.latitude, userLocation.longitude), 10f)
            map.animateCamera(cameraUpdate)

            map.setMapStyle(MapStyleOptions
                .loadRawResourceStyle(this@NearbyFragment.context, R.raw.map_style))

            getNearbyLocations()
        }
    }

    private fun getNearbyLocations() {
        val loadingDialog = LoadingDialog(requireContext(), "Loading locations, please wait.")
        loadingDialog.show()
        val miles: Int
        if(useMetric) {
            miles = Utils.distanceToImperial(currentSeekRadius.toDouble()).toInt()
        }
        else {
            miles = currentSeekRadius
        }

        viewModel
            .getNearbyLocations(userLocation.latitude, userLocation.longitude, miles)
            .observe(viewLifecycleOwner) {
                loadingDialog.dismiss()
                if(it.isNotEmpty()) {
                    constructMap(it)
                }
                else {
                    map.clear()

                    Toast
                        .makeText(this.context, "No locations found.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun constructMap(it: List<NiLocation>) {
        // Remove markers in the event that the range has been reduced and some or all of them
        // should no longer be visible.
        map.clear()

        // Go through the list of in-range locations and (re-)add them to the map.
        it.forEach { location ->
            val latLng = LatLng(location.lat.toDouble(), location.long.toDouble())
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(location.name)

            // The marker is added to the map but we are not done with it yet.
            // GoogleMap#addMarker returns the Marker that was added, so we save it and add
            // the backing NiLocation object to take the user to the attraction detail screen
            // when tapped. It is reduced to type Object but can be cast back to NiLocation when
            // retrieved.
            val marker = map.addMarker(markerOptions)
            marker.tag = location
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                goBack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goBack() {
        val mainActivity = activity as MainActivity
        mainActivity.supportFragmentManager.popBackStack()
    }
}
