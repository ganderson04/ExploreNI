package com.ganderson.exploreni.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.ganderson.exploreni.ui.activities.MainActivity

import com.ganderson.exploreni.R
import com.ganderson.exploreni.data.db.DbAccessor
import com.ganderson.exploreni.entities.api.NiLocation
import kotlinx.android.synthetic.main.fragment_attraction_detail.*

/**
 * A simple [Fragment] subclass.
 */
class AttractionDetailFragment(private val location: NiLocation,
                               private val cameFromMap: Boolean) : Fragment() {
    private lateinit var menu: Menu
    private lateinit var dbAccessor: DbAccessor

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
        dbAccessor = DbAccessor(context!!)

        // Loading spinner to be displayed while Glide loads the attraction image.
        val loadingSpinner = CircularProgressDrawable(this.activity!!)
        loadingSpinner.strokeWidth = 5f
        loadingSpinner.centerRadius = 30f
        loadingSpinner.start()

        // Load image asynchronously with Glide.
        Glide.with(this)
            .load(location.imgUrl)
            .placeholder(loadingSpinner)
            .error(R.drawable.placeholder_no_image_available)
            .into(ivAttraction)

        tvAttraction.text = location.desc + "\n" + location.imgAttr
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.attraction_toolbar, menu)
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isFavouriteLocation()) {
            val item = menu.findItem(R.id.tb_favourite)
            item.setIcon(context!!.getDrawable(R.drawable.ic_star_filled_white_24dp))
        }

        if(cameFromMap) {
            menu.findItem(R.id.tb_map).isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // This option is for the back arrow.
            android.R.id.home -> {
                goBack()
            }

            R.id.tb_favourite -> {
                if(!isFavouriteLocation()) {
                    addToFavourites()
                }
                else {
                    removeFromFavourites()
                }
                return true
            }

            R.id.tb_map -> {
                Toast.makeText(
                    this.activity!!, "View on map",
                    Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addToFavourites() {
        if(dbAccessor.addFavouriteLocation(location)) {
            Toast.makeText(this.activity!!, "Favourite added!",
                Toast.LENGTH_SHORT).show()
            val item = menu.findItem(R.id.tb_favourite)
            item.setIcon(R.drawable.ic_star_filled_white_24dp)
        }
    }

    private fun removeFromFavourites() {
        if(dbAccessor.removeFavouriteLocation(location.id)) {
            Toast.makeText(this.activity!!, "Favourite removed!",
                Toast.LENGTH_SHORT).show()
            val item = menu.findItem(R.id.tb_favourite)
            item.setIcon(R.drawable.ic_star_border_white_24dp)
        }
    }

    private fun isFavouriteLocation() : Boolean {
        return dbAccessor.isFavouriteLocation(location.id)
    }

    private fun goBack() {
        val mainActivity = activity as MainActivity
        mainActivity.supportFragmentManager.popBackStack()
    }
}
