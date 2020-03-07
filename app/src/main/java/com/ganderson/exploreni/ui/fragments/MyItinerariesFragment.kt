package com.ganderson.exploreni.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.viewmodels.MyItinerariesViewModel
import kotlinx.android.synthetic.main.fragment_my_itineraries.*

/**
 * A simple [Fragment] subclass.
 */
class MyItinerariesFragment : Fragment() {
    private val viewModel = MyItinerariesViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "My Itineraries"

        // Show the back button in the toolbar on sub-level screens.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_itineraries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getItineraries()
            .observe(viewLifecycleOwner) {
                val linearLayoutManager = LinearLayoutManager(this.context)
                val itinerariesAdapter = MyItinerariesAdapter(requireContext(), it)

                rvMyItineraries.layoutManager = linearLayoutManager
                rvMyItineraries.adapter = itinerariesAdapter
            }
    }

    class MyItinerariesAdapter(val context: Context, val itineraryList: List<Itinerary>)
        : RecyclerView.Adapter<MyItinerariesAdapter.ItineraryViewHolder>() {

        class ItineraryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cvMyItinerary = view.findViewById<CardView>(R.id.cvMyItinerary)
            val tvMyItineraryName = view.findViewById<TextView>(R.id.tvMyItineraryName)
            val tvMyItinerarySize = view.findViewById<TextView>(R.id.tvMyItinerarySize)
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
                Toast.makeText(context, "Tapped", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
