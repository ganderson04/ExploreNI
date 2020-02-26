package com.ganderson.exploreni.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.components.adapters.LocationAdapter
import com.ganderson.exploreni.ui.viewmodels.ExploreViewModel
import kotlinx.android.synthetic.main.fragment_explore_category.*

/**
 * A simple [Fragment] subclass.
 */
class ExploreCategoryFragment(private val locationType: LocationType) : Fragment() {
    private val viewModel = ExploreViewModel(locationType)
    private val sortOptions = arrayOf("A-Z", "Distance")

    private var locationManager: LocationManager? = null
    private var location: Location? = null
    private lateinit var sortDialog: Dialog
    private val locationList = ArrayList<NiLocation>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "View Locations"

        // States that this Fragment will set up its own toolbar menu.
        setHasOptionsMenu(true)

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (activity as MainActivity).getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager

            locationManager?.let {
                // "it" refers to locationManager. "let" blocks are similar to lambdas.
                if(it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else if(it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
            }

            // Create new Dialog only if this is a new instantiation. See the comment below.
            if(locationList.isEmpty()) createSortDialog()
        }

        // If the list is empty it must be a new instantiation of this fragment, so show the
        // loading dialog and begin observing the ViewModel's LiveData.
        if(locationList.isEmpty()) {
            val loadingDialog = LoadingDialog(requireContext(),
                "Loading locations, please wait.")
            loadingDialog.show()
            viewModel.locations.observe(viewLifecycleOwner) { list ->
                loadingDialog.dismiss()
                if (list.isNotEmpty()) {
                    locationList.clear()
                    locationList.addAll(list)
                    displayLocations()
                } else {
                    val alert = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Error")
                        .setMessage("No locations found for this type.")
                        .setPositiveButton("OK") { dialog, _ ->
                            run {
                                dialog.dismiss()
                                parentFragmentManager.popBackStack()
                            }
                        }
                    alert.show()
                }
            }
        }
        else {
            displayLocations()
        }
    }

    private fun displayLocations() {
        rvLocations.layoutManager = LinearLayoutManager(this.context)
        rvLocations.adapter = LocationAdapter(requireContext(), locationList)
    }

    private fun createSortDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Sort")
            .setSingleChoiceItems(sortOptions, 0) { dialog, item ->
                dialog.dismiss()
                sortList(item)
            }
            .create()
        sortDialog = dialog
    }

    private fun sortList(option: Int) {
        if(option == 0) locationList.sortBy { it.name }
        else locationList.sortWith(compareBy {
            Utils.getHaversineGCD(location!!.latitude, location!!.longitude,
                it.lat.toDouble(), it.long.toDouble())
        })
        rvLocations.adapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_category_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
            R.id.tb_sort -> sortDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
