package com.ganderson.exploreni.ui.fragments


import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.viewmodels.FavouritesViewModel
import kotlinx.android.synthetic.main.fragment_favourites.*

/**
 * A simple [Fragment] subclass.
 */
class FavouritesFragment : Fragment() {
    private val viewModel = FavouritesViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        viewModel.getFavouriteLocations()
            .observe(viewLifecycleOwner) {
                val linearLayoutManager = LinearLayoutManager(this.context)
                val favouritesAdapter = FavouritesAdapter(it,
                    object: FavouritesAdapter.OnRemoveClickListener {
                    override fun onRemoveClick(niLocation: NiLocation) {
                        removeFromFavourites(niLocation)
                    }
                })

                rvFavourites.layoutManager = linearLayoutManager
                rvFavourites.adapter = favouritesAdapter
            }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun removeFromFavourites(niLocation: NiLocation) {
        val dialog = AlertDialog.Builder(this.context)
            .setTitle("Confirm removal")
            .setMessage("Remove ${niLocation.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") {_, _ -> viewModel.removeFromFavourites(niLocation) }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        dialog.show()
    }

    class FavouritesAdapter(private val locations: List<NiLocation>,
                            private val removeClickListener: OnRemoveClickListener)
        : RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder>() {

        interface OnRemoveClickListener {
            fun onRemoveClick(niLocation: NiLocation)
        }

        class FavouritesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cvFavourite = view.findViewById<CardView>(R.id.cvFavourite)
            val ivFavourite = view.findViewById<ImageView>(R.id.ivFavourite)
            val tvFavouriteName = view.findViewById<TextView>(R.id.tvFavouriteName)
            val ibRemove = view.findViewById<ImageView>(R.id.ibRemove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_favourites_item, parent, false)
            return FavouritesViewHolder(view)
        }

        override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
            val location = locations[position]

            Glide.with(holder.view.context)
                .load(location.imgUrl)
                .centerCrop()
                .error(R.drawable.placeholder_no_image_available)
                .into(holder.ivFavourite)

            holder.tvFavouriteName.text = location.name
            holder.cvFavourite.setOnClickListener {
                val attractionDetailFragment = AttractionDetailFragment(location, false)
                val mainActivity = holder.view.context as MainActivity
                mainActivity.displayFragment(attractionDetailFragment)
            }
            holder.ibRemove.setOnClickListener {
                removeClickListener.onRemoveClick(location)
            }
        }

        override fun getItemCount() = locations.size
    }
}
