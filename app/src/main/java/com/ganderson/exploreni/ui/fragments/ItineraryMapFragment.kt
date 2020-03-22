package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

/**
 * A simple [Fragment] subclass.
 */
class ItineraryMapFragment(private val itinerary: Itinerary) : Fragment() {
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Itinerary Map"

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fcvMap = childFragmentManager.findFragmentById(R.id.fcvItineraryMap)
                as SupportMapFragment

        fcvMap.getMapAsync {
            this.map = it
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            map.setMapStyle(
                MapStyleOptions
                .loadRawResourceStyle(requireContext(), R.raw.map_style))

            addLocations()
            drawRoute()
        }
    }

    private fun addLocations() {
        for(i in 0 until itinerary.itemList.size) {
            val item = itinerary.itemList[i]
            val latLng = LatLng(item.lat.toDouble(), item.long.toDouble())
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("${i+1}. ${item.name}") // E.g. "1. Belfast Castle"
            map.addMarker(markerOptions)
        }
    }

    private fun drawRoute() {
        val polyline = PolylineOptions()
            .clickable(false)
        itinerary.itemList.forEach { item ->
            polyline.add(LatLng(item.lat.toDouble(), item.long.toDouble()))
        }
    }

}
