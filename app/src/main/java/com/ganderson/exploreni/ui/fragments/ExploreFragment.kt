package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.LocationType
import kotlinx.android.synthetic.main.fragment_explore.*

/**
 * A simple [Fragment] subclass.
 */
class ExploreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Explore"

        // Hide the back button in the toolbar on top-level menu options.
        // In this case, the back button is shown if the user came from the itinerary planner.
        // This will be the case if the target fragment, the fragment to which to return (i.e.
        // ItineraryViewerFragment), is not null.
        if(targetFragment != null) {
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setDisplayShowHomeEnabled(true)
        }
        else {
            actionBar?.setDisplayHomeAsUpEnabled(false)
            actionBar?.setDisplayShowHomeEnabled(false)
        }

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            val text = etSearch.text.toString().trim()
            if(text.isNotEmpty()) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(text)
                    return@setOnEditorActionListener true
                }
            }
            else {
                Toast.makeText(requireContext(), "Please enter a search term.",
                    Toast.LENGTH_SHORT).show()
            }
            return@setOnEditorActionListener false
        }

        cvSee.setOnClickListener {
            showCategory(LocationType.SEE)
        }

        cvDo.setOnClickListener {
            showCategory(LocationType.DO)
        }

        cvStay.setOnClickListener {
            showCategory(LocationType.STAY)
        }

        cvEat.setOnClickListener {
            showCategory(LocationType.EAT)
        }
    }

    private fun showCategory(locationType: LocationType) {
        val categoryFragment = ExploreCategoryFragment(locationType)
        categoryFragment.setTargetFragment(targetFragment, ADD_ITEM_CODE)

        val mainActivity = activity as MainActivity
        mainActivity.displayFragment(categoryFragment)
    }

    private fun performSearch(query: String) {
        val searchFragment = SearchFragment(query)
        searchFragment.setTargetFragment(targetFragment, ADD_ITEM_CODE)

        val mainActivity = activity as MainActivity
        mainActivity.displayFragment(searchFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}
