package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager

import com.ganderson.exploreni.R
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
    private val viewModel = ExploreViewModel()
    private lateinit var locationList: ArrayList<NiLocation>

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
        val loadingDialog = LoadingDialog(requireContext(),
            "Loading locations, please wait.")
        loadingDialog.show()
        viewModel.getLocations(locationType)
            .observe(viewLifecycleOwner) { list ->
                loadingDialog.dismiss()
                if(list.isNotEmpty()) {
                    locationList = ArrayList(list)
                    locationList.sortBy { it.name }

                    rvLocations.layoutManager = LinearLayoutManager(this.context)
                    rvLocations.adapter = LocationAdapter(requireContext(), locationList)
                }
                else {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}
