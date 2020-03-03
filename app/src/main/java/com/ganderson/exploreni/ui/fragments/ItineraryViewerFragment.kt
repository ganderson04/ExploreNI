package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_itinerary_viewer.*

/**
 * A simple [Fragment] subclass.
 */
class ItineraryViewerFragment(val isNew: Boolean) : Fragment() {

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
}
