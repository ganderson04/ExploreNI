package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import kotlinx.android.synthetic.main.fragment_plan.*

/**
 * A simple [Fragment] subclass.
 */
class PlanFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Plan"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnNewItinerary.setOnClickListener {
            val fragment = ItineraryViewerFragment(true)
            val mainActivity = activity as MainActivity
            mainActivity.displayFragment(fragment)
        }

        btnMyItineraries.setOnClickListener {
            val fragment = MyItinerariesFragment()
            val mainActivity = activity as MainActivity
            mainActivity.displayFragment(fragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
