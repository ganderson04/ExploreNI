package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

/**
 * A simple [Fragment] subclass.
 */
class AttractionMapFragment(private val location: NiLocation) : Fragment() {
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Attraction Location"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Declares that this fragment will set its own menu.
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attraction_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fcvMap = childFragmentManager.findFragmentById(R.id.fcvAttractionMap)
                as SupportMapFragment

        fcvMap.getMapAsync {
            this.map = it
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            val latLng = LatLng(location.lat.toDouble(), location.long.toDouble())
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
            map.animateCamera(cameraUpdate)
            map.setMapStyle(
                MapStyleOptions
                    .loadRawResourceStyle(requireContext(), R.raw.map_style))

            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(location.name)
            map.addMarker(markerOptions)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}
