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

class FavouriteAdapter(private val context: Context,
                       private val locations: List<NiLocation>,
                       private val removeClickListener: OnRemoveClickListener)
    : RecyclerView.Adapter<FavouriteAdapter.FavouritesViewHolder>() {

    interface OnRemoveClickListener {
        fun onRemoveClick(niLocation: NiLocation)
    }

    class FavouritesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cvFavourite = view.findViewById<CardView>(R.id.cvFavourite)
        val ivFavourite = view.findViewById<ImageView>(R.id.ivFavourite)
        val tvFavouriteName = view.findViewById<TextView>(R.id.tvFavouriteName)
        val ibRemove = view.findViewById<ImageView>(R.id.ibRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_favourites_item, parent, false)
        return FavouritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        val location = locations[position]

        Glide.with(context)
            .load(location.imgUrl)
            .centerCrop()
            .error(R.drawable.placeholder_no_image_available)
            .into(holder.ivFavourite)

        holder.tvFavouriteName.text = location.name
        holder.cvFavourite.setOnClickListener {
            val attractionDetailFragment = AttractionDetailFragment(location, false)
            val mainActivity = context as MainActivity
            mainActivity.displayFragment(attractionDetailFragment)
        }
        holder.ibRemove.setOnClickListener {
            removeClickListener.onRemoveClick(location)
        }
    }

    override fun getItemCount() = locations.size
}