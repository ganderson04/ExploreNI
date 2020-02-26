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

import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.components.adapters.EventAdapter
import com.ganderson.exploreni.ui.viewmodels.EventViewModel
import kotlinx.android.synthetic.main.fragment_event.*

/**
 * A simple [Fragment] subclass.
 */
class EventFragment : Fragment() {
    private val viewModel = EventViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Events"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        // Declares that this fragment will set its own menu.
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loadingDialog = LoadingDialog(requireContext(), "Loading events, please wait.")
        loadingDialog.show()
        viewModel.getEvents()
            .observe(viewLifecycleOwner) {
                loadingDialog.dismiss()
                if(it.isNotEmpty()) {
                    val linearLayoutManager = LinearLayoutManager(this.context)
                    val eventAdapter = EventAdapter(requireContext(), it)

                    rvEvents.layoutManager = linearLayoutManager
                    rvEvents.adapter = eventAdapter
                }
                else {
                    val alert = AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Error")
                        .setMessage("No events found.")
                        .setPositiveButton("OK") { dialog, _ ->
                            run {
                                dialog.dismiss()
                                parentFragmentManager.popBackStack()
                            }
                        }
                    alert.show()
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}
