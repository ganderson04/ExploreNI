package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.components.adapters.LocationAdapter
import com.ganderson.exploreni.ui.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment(private val query: String) : Fragment() {
    private val viewModel: SearchViewModel by viewModels()
    private val locationList = ArrayList<NiLocation>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Results for \"$query\""

        // States that this Fragment will set up its own toolbar menu.
        setHasOptionsMenu(true)

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If the list is empty, we must have come from the "Explore" screen and it's a new search.
        if(locationList.isEmpty()) {
            EspressoIdlingResource.increment()
            val loadingDialog = LoadingDialog(requireContext(), "Searching...")
            loadingDialog.show()
            viewModel.performSearch(query)
            viewModel
                .searchResults
                .observe(viewLifecycleOwner) { listResult ->
                    loadingDialog.dismiss()
                    EspressoIdlingResource.decrement()

                    if (listResult.data != null) {
                        locationList.addAll(listResult.data)
                        if (locationList.isNotEmpty()) {
                            displayLocations()
                        }
                        else {
                            val alert = AlertDialog.Builder(requireContext())
                                .setCancelable(false)
                                .setTitle("No results")
                                .setMessage("Please try another search term.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    run {
                                        dialog.dismiss()
                                        parentFragmentManager.popBackStack()
                                    }
                                }
                            alert.show()
                        }
                    }
                    else {
                        val alert = AlertDialog.Builder(requireContext())
                            .setCancelable(false)
                            .setTitle("Error")
                            .setMessage("Unable to load search results.")
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
        locationList.sortBy { it.name }

        val adapterListener = object : LocationAdapter.OnLocationClickListener {
            override fun onLocationClick(location: NiLocation) {
                val mainActivity = activity as MainActivity
                val attractionDetailFragment = AttractionDetailFragment(location,
                    false)
                mainActivity.displayFragment(attractionDetailFragment)
            }
        }

        rvSearchResults.layoutManager = LinearLayoutManager(this.context)
        rvSearchResults.adapter = LocationAdapter(
            requireContext(), locationList,
            adapterListener
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}
