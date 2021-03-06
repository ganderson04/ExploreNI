package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.components.adapters.LocationAdapter
import com.ganderson.exploreni.ui.viewmodels.ExploreViewModel
import kotlinx.android.synthetic.main.fragment_explore_category.*
import java.util.stream.Collectors

class ExploreCategoryFragment(private val locationType: LocationType) : Fragment() {
    private val viewModel: ExploreViewModel by viewModels()

    private val sortOptions = arrayOf("A-Z", "Distance")
    private val locationList = ArrayList<NiLocation>()
    private val currentFilterList = ArrayList<String>()

    private lateinit var sortDialog: Dialog
    private lateinit var filterDialog: Dialog
    private lateinit var adapterListener: LocationAdapter.OnLocationClickListener

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
        rvLocations.layoutManager = LinearLayoutManager(requireContext())

        if(locationList.isEmpty()) createSortDialog()

        // If the list is empty it must be a new instantiation of this fragment, so show the
        // loading dialog and begin observing the ViewModel's LiveData.
        if(locationList.isEmpty()) {
            // Beginning asynchronous activity. If testing, tell Espresso to wait.
            EspressoIdlingResource.increment()
            val loadingDialog = LoadingDialog(requireContext(),
                "Loading locations, please wait.")
            loadingDialog.show()

            viewModel.setLocationType(locationType)
            viewModel
                .locations
                .observe(viewLifecycleOwner) { listResult ->
                    loadingDialog.dismiss()
                    // If testing, tell Espresso that the asynchronous activity is finished.
                    EspressoIdlingResource.decrement()

                    if(listResult.data != null) {
                        val list = listResult.data
                        if (list.isNotEmpty()) {
                            locationList.clear()
                            locationList.addAll(list)
                            displayLocations()
                            createFilterDialog()
                        }
                        else {
                            val alert = AlertDialog.Builder(requireContext())
                                .setCancelable(false)
                                .setTitle("Error")
                                .setMessage("No locations found for this type.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                    parentFragmentManager.popBackStack()
                                }
                            alert.show()
                        }
                    }
                    else {
                        val alert = AlertDialog.Builder(requireContext())
                            .setCancelable(false)
                            .setTitle("Error")
                            .setMessage("Unable to load locations.")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                parentFragmentManager.popBackStack()
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
        if(currentFilterList.isNotEmpty()) {
            filterList()
        }
        else {
            adapterListener = object: LocationAdapter.OnLocationClickListener {
                override fun onLocationClick(location: NiLocation) {
                    val mainActivity = activity as MainActivity
                    if(targetFragment != null) {
                        (targetFragment as ItineraryViewerFragment).addItem(location)
                        mainActivity.displayFragment(targetFragment!!)
                    }
                    else {
                        val attractionDetailFragment = AttractionDetailFragment(location,
                            false)
                        mainActivity.displayFragment(attractionDetailFragment)
                    }
                }
            }
            rvLocations.adapter = LocationAdapter(requireContext(), locationList, adapterListener)
        }
    }

    private fun getDistinctTags() : List<String> {
        val allTags = ArrayList<String>()
        val distinctTagList = ArrayList<String>()
        locationList.forEach { location -> allTags.addAll(location.locTags) }

        // Utilises Java 8 Streams.
        distinctTagList.addAll(allTags.stream().distinct().collect(Collectors.toList()))
        return distinctTagList
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

    private fun createFilterDialog() {
        val tagList = getDistinctTags()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Filter")
            .setCancelable(false)
            .setMultiChoiceItems(tagList.toTypedArray(), null) { _, which, isChecked ->
                val item = tagList[which]
                if(isChecked && !currentFilterList.contains(item)) {
                    currentFilterList.add(item)
                }
                else if(currentFilterList.contains(item)) {
                    currentFilterList.remove(item)
                }
            }
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Clear all") { dialog, _ ->
                (dialog as AlertDialog).listView.clearChoices()
                currentFilterList.clear()
                dialog.dismiss()
            }
            .setOnDismissListener {
                displayLocations()
            }
            .create()
        filterDialog = dialog
    }

    private fun sortList(option: Int) {
        if(option == 0) locationList.sortBy { it.name }
        else {
            val lastLocation = (activity as MainActivity).getLastLocation()
            if(lastLocation != null) {
                locationList.sortWith(
                    compareBy { niLocation ->
                        Utils.getHaversineGCD(
                            lastLocation.latitude, lastLocation.longitude,
                            niLocation.lat.toDouble(), niLocation.long.toDouble()
                        )
                    }
                )
            }
            else {
                Toast.makeText(requireContext(), "Location currently unavailable. Sorting A-Z.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        rvLocations.adapter?.notifyDataSetChanged()
    }

    private fun filterList() {
        val filteredLocations = locationList.filter { location ->
            location.locTags.forEach { tag ->
                if (currentFilterList.contains(tag)) return@filter true
            }
            return@filter false
        }
        rvLocations.adapter = LocationAdapter(requireContext(), filteredLocations, adapterListener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_explore_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
            R.id.tb_sort -> sortDialog.show()
            R.id.tb_filter -> filterDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
