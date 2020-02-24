package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager

import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.adapters.EventAdapter
import com.ganderson.exploreni.ui.viewmodels.EventViewModel
import kotlinx.android.synthetic.main.fragment_event.*

/**
 * A simple [Fragment] subclass.
 */
class EventFragment : Fragment() {
    private val viewModel = EventViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Events"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Declares that this fragment will set its own menu.
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getEvents()
            .observe(viewLifecycleOwner) {
                val linearLayoutManager = LinearLayoutManager(this.context)
                val eventAdapter = EventAdapter(requireContext(), it)

                rvEvents.layoutManager = linearLayoutManager
                rvEvents.adapter = eventAdapter
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goBack() {
        val mainActivity = this.activity as MainActivity
        mainActivity.supportFragmentManager.popBackStack()
    }

//    class EventAdapter(private val events: List<Event>)
//        : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
//        private val eventDateFormatter = DateTimeFormatter.ofPattern("E d MMM")
//
//        class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
//            val cvEvent = view.findViewById<CardView>(R.id.cvEvent)
//            val ivEvent = view.findViewById<ImageView>(R.id.ivEvent)
//            val tvEventName = view.findViewById<TextView>(R.id.tvEventName)
//            val tvEventDates = view.findViewById<TextView>(R.id.tvEventDates)
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.layout_event_item, parent, false)
//            return EventViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
//            val event = events[position]
//
//            Glide.with(holder.view.context)
//                .load(event.imgUrl)
//                .centerCrop()
//                .error(R.drawable.placeholder_no_image_available)
//                .into(holder.ivEvent)
//
//            val dateString = "${event.startDate.format(eventDateFormatter)} - ${event.endDate.format(eventDateFormatter)}"
//
//            holder.tvEventName.text = event.name
//            holder.tvEventDates.text = dateString
//            holder.cvEvent.setOnClickListener {
//                val eventDetailFragment = EventDetailFragment(event)
//                val mainActivity = holder.view.context as MainActivity
//                mainActivity.displayFragment(eventDetailFragment)
//            }
//        }
//
//        override fun getItemCount() = events.size
//    }
}
