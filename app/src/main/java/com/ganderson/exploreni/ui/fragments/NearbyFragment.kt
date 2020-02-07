package com.ganderson.exploreni.ui.fragments


import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.viewmodels.NearbyViewModel
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

/**
 * A simple [Fragment] subclass.
 */
class NearbyFragment(private val userLocation: Location) : Fragment() {
    private val viewModel: NearbyViewModel =
        NearbyViewModel()
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
        val loadingDialog = LoadingDialog(context!!, "Loading locations, please wait.")
        loadingDialog.show()

        viewModel
            .getNearbyLocations(userLocation.latitude, userLocation.longitude)
            .observe(viewLifecycleOwner) {
                loadingDialog.dismiss()
                constructMap(it)
            }
    }

    private fun constructMap(it: List<NiLocation>) {
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
