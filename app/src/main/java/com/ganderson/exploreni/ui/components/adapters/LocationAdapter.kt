package com.ganderson.exploreni.ui.components.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.AttractionDetailFragment

class LocationAdapter(private val context: Context, private val locations: List<NiLocation>)
    : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cvLocation = view.findViewById<CardView>(R.id.cvLocation)
        val ivLocation = view.findViewById<ImageView>(R.id.ivLocation)
        val tvLocationName = view.findViewById<TextView>(R.id.tvLocationName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_location_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]

        Glide.with(context)
            .load(location.imgUrl)
            .centerCrop()
            .error(R.drawable.placeholder_no_image_available)
            .into(holder.ivLocation)

        holder.tvLocationName.text = location.name
        holder.cvLocation.setOnClickListener {
            val attractionDetailFragment = AttractionDetailFragment(location, false)
            val mainActivity = context as MainActivity
            mainActivity.displayFragment(attractionDetailFragment)
        }
    }

    override fun getItemCount() = locations.size
}