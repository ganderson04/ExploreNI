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
import com.ganderson.exploreni.entities.data.api.Event
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.fragments.EventDetailFragment
import java.time.format.DateTimeFormatter

class EventAdapter(private val context: Context, private val events: List<Event>)
    : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    private val eventDateFormatter = DateTimeFormatter.ofPattern("E d MMM")

    class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cvEvent = view.findViewById<CardView>(R.id.cvEvent)
        val ivEvent = view.findViewById<ImageView>(R.id.ivEvent)
        val tvEventName = view.findViewById<TextView>(R.id.tvEventName)
        val tvEventDates = view.findViewById<TextView>(R.id.tvEventDates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        Glide.with(context)
            .load(event.imgUrl)
            .centerCrop()
            .error(R.drawable.placeholder_no_image_available)
            .into(holder.ivEvent)

        val dateString = "${event.startDate.format(eventDateFormatter)} - ${event.endDate.format(eventDateFormatter)}"

        holder.tvEventName.text = event.name
        holder.tvEventDates.text = dateString
        holder.cvEvent.setOnClickListener {
            val eventDetailFragment = EventDetailFragment(event)
            val mainActivity = context as MainActivity
            mainActivity.displayFragment(eventDetailFragment)
        }
    }

    override fun getItemCount() = events.size
}