package com.ganderson.exploreni.ui.fragments


import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.api.services.ExploreService
import com.ganderson.exploreni.models.NiLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A simple [Fragment] subclass.
 */
class NearbyFragment(private val userLocation: Location) : Fragment() {
    private val exploreService by lazy { setupExploreService() }
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

    private fun setupExploreService() : ExploreService {
        val gson = GsonBuilder()
            .registerTypeAdapter(NiLocation::class.java, ExploreService.LocationDeserialiser())
            .create()

        return Retrofit.Builder()
            .baseUrl(ExploreService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ExploreService::class.java)
    }

    private fun getNearbyLocations() {
        val nearbyCall = exploreService.getNearbyLocations(userLocation.latitude,
            userLocation.longitude)
        nearbyCall.enqueue(object: Callback<List<NiLocation>> {
            override fun onResponse(call: Call<List<NiLocation>>,
                                    response: Response<List<NiLocation>>) {
                response.body()?.let {
                    constructMap(it)
                }
            }

            override fun onFailure(call: Call<List<NiLocation>>, t: Throwable) {
                Toast.makeText(this@NearbyFragment.context,
                    "Unable to load locations.", Toast.LENGTH_SHORT).show()
            }
        })
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
