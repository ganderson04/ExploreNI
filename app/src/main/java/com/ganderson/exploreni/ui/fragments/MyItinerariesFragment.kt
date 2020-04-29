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
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.adapters.MyItinerariesAdapter
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

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_itineraries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EspressoIdlingResource.increment()
        val onRemoveClickListener = object: MyItinerariesAdapter.OnRemoveClickListener {
            override fun onRemoveClick(itinerary: Itinerary) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Confirm removal")
                    .setMessage("Remove \"${itinerary.name}\"?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ -> viewModel.deleteItinerary(itinerary.dbId) }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                dialog.show()
            }
        }
        viewModel.getItineraries()
            .observe(viewLifecycleOwner) { list ->
                val sortedList = list.sortedBy { itinerary -> itinerary.name }
                val linearLayoutManager = LinearLayoutManager(this.context)
                val itinerariesAdapter = MyItinerariesAdapter(requireContext(), sortedList,
                    onRemoveClickListener)

                rvMyItineraries.layoutManager = linearLayoutManager
                rvMyItineraries.adapter = itinerariesAdapter
                EspressoIdlingResource.decrement()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val mainActivity = activity as MainActivity
                mainActivity.displayFragment(PlanFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
