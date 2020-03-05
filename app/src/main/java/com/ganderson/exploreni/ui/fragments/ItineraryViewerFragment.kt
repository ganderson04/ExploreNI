package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_itinerary_viewer.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ItineraryViewerFragment(val isNew: Boolean) : Fragment() {
    val itemList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obtain the toolbar via the Fragment's underlying Activity. This must first be cast
        // as an object of MainActivity.
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = "Itinerary Viewer"

        // Hide the back button in the toolbar on top-level menu options.
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itinerary_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvItineraryName.setOnClickListener { showInputDialog() }
    }

    private fun showInputDialog() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        val etInput = EditText(requireContext()).apply {
            isFocusable = true
            inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            append(tvItineraryName.text)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change itinerary name")
            .setView(etInput)
            .setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                val name = etInput.text.toString()
                if(name.isNotBlank()) {
                    tvItineraryName.text = name
                }

                etInput.clearFocus()
                imm.hideSoftInputFromWindow(etInput.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
            .create()

        dialog.setOnShowListener {
            etInput.requestFocus()
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        etInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.dismiss()
            }
            true
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.itinerary_viewer_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isNew) {
            val item = menu.findItem(R.id.tb_edit_itinerary)
            item.isVisible = false
        }
        else {
            val item = menu.findItem(R.id.tb_add_location)
            item.isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> parentFragmentManager.popBackStack()
            R.id.tb_add_location -> add()
            R.id.tb_edit_itinerary -> Toast
                .makeText(requireContext(), "Edit", Toast.LENGTH_SHORT).show()
            R.id.tb_itinerary_map -> Toast
                .makeText(requireContext(), "Map", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun add() {

    }

    class ItineraryAdapter(val context: Context, val itemList: ArrayList<String>)
        : RecyclerView.Adapter<ItineraryAdapter.ItemViewHolder>() {

        class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cvItineraryItem = view.findViewById<CardView>(R.id.cvItineraryItem)
            val tvItineraryItem = view.findViewById<TextView>(R.id.tvItineraryItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_itinerary_item,
                parent, false)
            return ItemViewHolder(view)
        }

        override fun getItemCount() = itemList.size

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.tvItineraryItem.text = itemList[position]
        }
    }
}
