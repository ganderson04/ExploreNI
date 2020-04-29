package com.ganderson.exploreni.ui.components.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.data.api.NiLocation

class ItineraryAdapter(val context: Context, private val itemList: List<NiLocation>,
                       private val removeListener: OnRemoveClickListener)
    : RecyclerView.Adapter<ItineraryAdapter.ItemViewHolder>() {

    interface OnRemoveClickListener {
        fun onRemoveClick(itemIndex: Int)
    }

    class ItemViewHolder(val view: View, val removeListener: OnRemoveClickListener)
        : RecyclerView.ViewHolder(view) {
        val tvItineraryItem = view.findViewById<TextView>(R.id.tvItineraryItem)
        val ivItineraryItem = view.findViewById<ImageView>(R.id.ivItineraryLocation)
        val ibRemoveItem = view.findViewById<ImageButton>(R.id.ibRemoveItem)

        init {
            ibRemoveItem.setOnClickListener { removeListener.onRemoveClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.layout_itinerary_item,
            parent, false)
        return ItemViewHolder(view, removeListener)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        Glide.with(context)
            .load(item.imgUrl)
            .centerCrop()
            .error(R.drawable.placeholder_no_image_available)
            .into(holder.ivItineraryItem)

        holder.tvItineraryItem.text = item.name
    }
}