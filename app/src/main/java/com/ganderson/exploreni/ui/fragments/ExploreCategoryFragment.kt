package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.LocationType
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.viewmodels.ExploreViewModel
import kotlinx.android.synthetic.main.fragment_explore_category.*

/**
 * A simple [Fragment] subclass.
 */
class ExploreCategoryFragment(private val locationType: LocationType) : Fragment() {
    private val viewModel = ExploreViewModel()

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
        viewModel.getLocations(locationType)
            .observe(viewLifecycleOwner) {
                val linearLayoutManager = LinearLayoutManager(this.context,
                    LinearLayoutManager.VERTICAL, false)
                val locationAdapter = LocationAdapter(it.sortedBy { it.name })

                rvLocations.layoutManager = linearLayoutManager
                rvLocations.adapter = locationAdapter
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

    class LocationAdapter(private val locations: List<NiLocation>)
        : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

        class LocationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cvLocation = view.findViewById<CardView>(R.id.cvLocation)
            val ivLocation = view.findViewById<ImageView>(R.id.ivLocation)
            val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_location_item, parent, false)
            return LocationViewHolder(view)
        }

        override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
            val location = locations[position]

            Glide.with(holder.view.context)
                .load(location.imgUrl)
                .centerCrop()
                .error(R.drawable.placeholder_no_image_available)
                .into(holder.ivLocation)

            holder.tvLocationName.text = location.name
            holder.cvLocation.setOnClickListener {
                val attractionDetailFragment = AttractionDetailFragment(location, false)
                val mainActivity = holder.view.context as MainActivity
                mainActivity.displayFragment(attractionDetailFragment)
            }
        }

        override fun getItemCount() = locations.size
    }
}
