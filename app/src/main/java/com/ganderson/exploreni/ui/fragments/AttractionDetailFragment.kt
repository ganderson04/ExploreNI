package com.ganderson.exploreni.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.viewmodels.AttractionDetailViewModel
import kotlinx.android.synthetic.main.fragment_attraction_detail.*

class AttractionDetailFragment(private val location: NiLocation,
                               private val cameFromMap: Boolean) : Fragment() {
    private val viewModel: AttractionDetailViewModel by viewModels()
    private lateinit var menu: Menu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = location.name

        // Show and enable back arrow in the toolbar.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // States that this Fragment will set up its own toolbar menu.
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_attraction_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Loading spinner to be displayed while Glide loads the attraction image.
        val loadingSpinner = CircularProgressDrawable(requireContext())
        loadingSpinner.strokeWidth = 5f
        loadingSpinner.centerRadius = 30f
        loadingSpinner.start()

        // Load image asynchronously with Glide.
        Glide.with(this)
            .load(location.imgUrl)
            .placeholder(loadingSpinner)
            .error(R.drawable.placeholder_no_image_available)
            .into(ivAttraction)

        val attractionText = "${location.desc}\n\n${location.imgAttr}"
        tvAttraction.text = attractionText

        btnWebsite.setOnClickListener { openWebsite() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_attraction, menu)
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isFavouriteLocation()) {
            val item = menu.findItem(R.id.tb_favourite)
            item.icon = requireContext().getDrawable(R.drawable.ic_star_filled_white_24dp)
        }

        if(cameFromMap) {
            menu.findItem(R.id.tb_map).isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // This option is for the back arrow.
            android.R.id.home -> parentFragmentManager.popBackStack()

            R.id.tb_favourite -> {
                if(!isFavouriteLocation()) {
                    addToFavourites()
                }
                else {
                    removeFromFavourites()
                }
            }

            R.id.tb_map -> {
                val mainActivity = requireActivity() as MainActivity
                mainActivity.displayFragment(AttractionMapFragment(location))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openWebsite() {
        // Intents are used to start Activities and can pass data along. In this case, the URL
        // of the event's website.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(location.website))
        startActivity(intent)
    }

    private fun addToFavourites() {
        if(viewModel.addFavouriteLocation(location)) {
            Toast.makeText(requireContext(), "Favourite added!",
                Toast.LENGTH_SHORT).show()
            val item = menu.findItem(R.id.tb_favourite)
            item.setIcon(R.drawable.ic_star_filled_white_24dp)
        }
        else {
            Toast.makeText(requireContext(), "Error adding favourite.",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromFavourites() {
        if(viewModel.removeFavouriteLocation(location.id)) {
            Toast.makeText(requireContext(), "Favourite removed!",
                Toast.LENGTH_SHORT).show()
            val item = menu.findItem(R.id.tb_favourite)
            item.setIcon(R.drawable.ic_star_border_white_24dp)
        }
        else {
            Toast.makeText(requireContext(), "Error removing favourite.",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun isFavouriteLocation() : Boolean {
        return viewModel.isFavouriteLocation(location.id)
    }
}
