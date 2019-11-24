package com.ganderson.exploreni.fragments


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.ganderson.exploreni.MainActivity

import com.ganderson.exploreni.R

/**
 * A simple [Fragment] subclass.
 */
class FavouritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.title = "Favourites"
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
