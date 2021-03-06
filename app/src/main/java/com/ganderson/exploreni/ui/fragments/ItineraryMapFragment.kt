package com.ganderson.exploreni.ui.fragments

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.viewmodels.ItineraryMapViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil

class ItineraryMapFragment(private val itinerary: Itinerary) : Fragment() {
    private val viewModel: ItineraryMapViewModel by viewModels()
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
        fcvMap.getMapAsync { googleMap ->
            this.map = googleMap
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

            val userLocation = getUserLocation()
            val cameraUpdate: CameraUpdate
            if(userLocation != null) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.latitude, userLocation.longitude), 10f
                )
            }
            else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatLng(itinerary.itemList[0].lat.toDouble(),
                        itinerary.itemList[0].long.toDouble()), 10f
                )
            }

            map.animateCamera(cameraUpdate)
            map.setMapStyle(
                MapStyleOptions
                .loadRawResourceStyle(requireContext(), R.raw.map_style))

            addLocations()
            drawRoute()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
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
        EspressoIdlingResource.increment()

        val loadingDialog = LoadingDialog(requireContext(), "Loading route, please wait.")
        loadingDialog.show()
        viewModel.setPolylineParams(itinerary, getUserLocation(),
                resources.getString(R.string.google_api_key))
        viewModel
            .polyline
            .observe(viewLifecycleOwner) { polyStringResult ->
                if(polyStringResult.data != null) {
                    val polyString = polyStringResult.data
                    val polyline = PolylineOptions()
                        .clickable(false)
                    polyline.color(resources.getColor(R.color.colorPrimaryDark, null))
                    polyline.addAll(PolyUtil.decode(polyString))
                    map.addPolyline(polyline)
                }
                else {
                    Toast.makeText(requireContext(), "Unable to load route.",
                        Toast.LENGTH_SHORT).show()
                }
                loadingDialog.dismiss()
                EspressoIdlingResource.decrement()
            }
    }

    private fun getUserLocation() : Location? {
        return (activity as MainActivity).getLastLocation()
    }
}
