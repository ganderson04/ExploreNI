package com.ganderson.exploreni.ui.components.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.ItineraryViewerFragment

class MyItinerariesAdapter(val context: Context, private val itineraryList: List<Itinerary>,
                           private val listener: OnRemoveClickListener)
    : RecyclerView.Adapter<MyItinerariesAdapter.ItineraryViewHolder>() {

    interface OnRemoveClickListener {
        fun onRemoveClick(itinerary: Itinerary)
    }

    class ItineraryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cvMyItinerary = view.findViewById<CardView>(R.id.cvMyItinerary)
        val tvMyItineraryName = view.findViewById<TextView>(R.id.tvMyItineraryName)
        val tvMyItinerarySize = view.findViewById<TextView>(R.id.tvMyItinerarySize)
        val ibRemoveItinerary = view.findViewById<ImageButton>(R.id.ibRemoveItinerary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ItineraryViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_my_itineraries_item, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun getItemCount(): Int = itineraryList.size

    override fun onBindViewHolder(holder: ItineraryViewHolder,
                                  position: Int) {
        val itinerary = itineraryList[position]
        holder.tvMyItineraryName.text = itinerary.name

        var sizeText = "${itinerary.itemList.size} location"
        if(itinerary.itemList.size > 1) sizeText += "s"

        holder.tvMyItinerarySize.text = sizeText

        holder.cvMyItinerary.setOnClickListener {
            val mainActivity = context as MainActivity
            mainActivity.displayFragment(ItineraryViewerFragment(false, itinerary))
        }

        holder.ibRemoveItinerary.setOnClickListener { listener.onRemoveClick(itinerary) }
    }
}