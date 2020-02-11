package com.ganderson.exploreni.ui.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.api.Event
import com.ganderson.exploreni.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_event_detail.*

/**
 * A simple [Fragment] subclass.
 */
class EventDetailFragment(private val event: Event) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = event.name

        // Show and enable back arrow in the toolbar.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // States that this Fragment will set up its own toolbar menu.
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Loading spinner to be displayed while Glide loads the attraction image.
        val loadingSpinner = CircularProgressDrawable(this.activity!!)
        loadingSpinner.strokeWidth = 5f
        loadingSpinner.centerRadius = 30f
        loadingSpinner.start()

        Glide.with(this)
            .load(event.imgUrl)
            .centerCrop()
            .placeholder(loadingSpinner)
            .error(R.drawable.placeholder_no_image_available)
            .into(ivEvent)

        tvEventDesc.text = event.desc

        btnWebsite.setOnClickListener {
            openWebsite()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                goBack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goBack() {
        val mainActivity = this.activity as MainActivity
        mainActivity.supportFragmentManager.popBackStack()
    }

    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.website))
        startActivity(intent)
    }
}
