package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganderson.exploreni.EspressoIdlingResource
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.components.adapters.FavouritesAdapter
import com.ganderson.exploreni.ui.viewmodels.FavouritesViewModel
import kotlinx.android.synthetic.main.fragment_favourites.*

class FavouritesFragment : Fragment() {
    private val viewModel: FavouritesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Favourites"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        EspressoIdlingResource.increment()
        viewModel
            .favourites
            .observe(viewLifecycleOwner) { favouritesResult ->
                EspressoIdlingResource.decrement()
                if(favouritesResult.data != null) {
                    val linearLayoutManager = LinearLayoutManager(requireContext())
                    val favouritesAdapter = FavouritesAdapter(requireContext(),
                        favouritesResult.data,
                        object : FavouritesAdapter.OnRemoveClickListener {
                            override fun onRemoveClick(niLocation: NiLocation) {
                                removeFromFavourites(niLocation)
                            }
                        })

                    rvFavourites.layoutManager = linearLayoutManager
                    rvFavourites.adapter = favouritesAdapter
                }
                else {
                    Toast.makeText(requireContext(), "Error loading favourites.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun removeFromFavourites(niLocation: NiLocation) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm removal")
            .setMessage("Remove ${niLocation.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") {_, _ ->
                if(!viewModel.removeFromFavourites(niLocation)) {
                    Toast.makeText(requireContext(), "Error removing favourite.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        dialog.show()
    }
}
