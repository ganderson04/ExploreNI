package com.ganderson.exploreni.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.ItineraryItemTouchCallback
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.components.adapters.ItineraryAdapter
import com.ganderson.exploreni.ui.viewmodels.ItineraryViewerViewModel
import kotlinx.android.synthetic.main.fragment_itinerary_viewer.*

const val ADD_ITEM_CODE = 1 // Used when accessing the "Explore" feature to choose a location.

class ItineraryViewerFragment(private val isNew: Boolean, savedItinerary: Itinerary?) : Fragment() {
    private val viewModel: ItineraryViewerViewModel by viewModels()
    private val itinerary = savedItinerary ?: Itinerary()

    private lateinit var durationLoadingDialog: LoadingDialog
    private var newNameChanged = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
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
        durationLoadingDialog = LoadingDialog(requireContext(),
            "Calculating duration, please wait.")

        // It was possible, through use of the app's back button in the toolbar or the phone's
        // back button, to return to a deleted itinerary in the itinerary viewer. This meant the
        // app would crash if the user pressed the app's back button which would activate the
        // saving procedure. Since the itinerary would already have a database ID, the app would
        // attempt to update its record in the database which would no longer exist.
        // The if-statement below checks if the itinerary has a database ID already but its name
        // does not match any itinerary currently stored in the database. If an itinerary cannot
        // be found by that name then it must have been deleted. The user is then taken back to
        // the "Plan" screen.
        if(itinerary.dbId.isNotEmpty() &&
            !ExploreRepository.isDuplicateItineraryName(itinerary.name)){
                alertDeletedItinerary()
        }
        else {
            tvItineraryName.setOnClickListener { showInputDialog() }
            if (itinerary.itemList.isNotEmpty()) {
                tvTapAdd.visibility = View.GONE
                rvItinerary.visibility = View.VISIBLE
                calculateDuration()
            }

            val itemMovedCallback = object : ItineraryItemTouchCallback.ItemMovedCallback {
                override fun itemMoved() {
                    calculateDuration()
                }
            }
            val itemTouchHelper = ItemTouchHelper(
                ItineraryItemTouchCallback(
                    itinerary.itemList,
                    itemMovedCallback
                )
            )
            itemTouchHelper.attachToRecyclerView(rvItinerary)

            val removeListener = object : ItineraryAdapter.OnRemoveClickListener {
                override fun onRemoveClick(itemIndex: Int) {
                    confirmItemRemoval(itemIndex)
                }
            }
            rvItinerary.layoutManager = LinearLayoutManager(requireContext())
            rvItinerary.adapter =
                ItineraryAdapter(requireContext(), itinerary.itemList, removeListener)

            if(isNew && !newNameChanged) {
                showInputDialog()
            }
        }

        viewModel
            .duration
            .observe(viewLifecycleOwner) { secondsResult ->
                durationLoadingDialog.dismiss()
                EspressoIdlingResource.decrement()
                if(secondsResult.data != null) {
                    val duration = Utils.secondsToTimeString(secondsResult.data)
                    val durationText = "Travel time: $duration"
                    tvItineraryDuration.text = durationText
                }
                else {
                    Toast.makeText(requireContext(), "Unable to calculate duration.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun alertDeletedItinerary() {
        AlertDialog.Builder(requireContext())
            .setTitle("Warning")
            .setMessage("Itinerary has been deleted.")
            .setPositiveButton("OK") {dialog, _ ->
                dialog.dismiss()
                val mainActivity = this.activity as MainActivity
                mainActivity.displayFragment(PlanFragment())
            }
            .show()
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
            .setCancelable(false)
            .setTitle("Change itinerary name")
            .setView(etInput)
            .setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                val name = etInput.text.toString()
                if(name.isNotBlank()) {
                    changeItineraryName(name)
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

    private fun changeItineraryName(name: String) {
        if((isNew && !newNameChanged) || name != itinerary.name) {
            if (viewModel.isDuplicateItineraryName(name)) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setCancelable(true)
                    .setTitle("Error")
                    .setMessage("That itinerary name already exists.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        showInputDialog()
                    }
                    .create()
                dialog.show()
            } else {
                itinerary.name = name
                tvItineraryName.text = name
                newNameChanged = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.itinerary_viewer_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        tvItineraryName.text = itinerary.name
        rvItinerary.adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                goBack(false)
            }
            R.id.tb_add_location -> goToExplore()
            R.id.tb_itinerary_map -> goToMap()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goBack(wasDeleted: Boolean) {
        // Save on close only if it's a new, non-empty itinerary or if the user chose to keep the
        // itinerary after removing all of its items.
        if((isNew && itinerary.itemList.isNotEmpty()) || (!isNew && !wasDeleted)) {
            if (viewModel.saveItinerary(itinerary)) {
                Toast.makeText(requireContext(), "Itinerary saved.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Error saving itinerary.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        val mainActivity = activity as MainActivity
        if(isNew) {
            mainActivity.displayFragment(PlanFragment())
        }
        else mainActivity.displayFragment(MyItinerariesFragment())
    }

    private fun goToMap() {
        if(itinerary.itemList.isNotEmpty()) {
            val mainActivity = activity as MainActivity
            mainActivity.displayFragment(ItineraryMapFragment(itinerary))
        }
        else {
            Toast
                .makeText(requireContext(), "No locations to show.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun goToExplore() {
        val exploreFragment = ExploreFragment()
        val mainActivity = activity as MainActivity

        exploreFragment.setTargetFragment(this, ADD_ITEM_CODE)
        mainActivity.displayFragment(exploreFragment)
    }

    fun addItem(item: NiLocation) {
        itinerary.itemList.add(item)
    }

    private fun confirmItemRemoval(itemIndex: Int) {
        val dialog = AlertDialog.Builder(this.context)
            .setTitle("Confirm removal")
            .setMessage("Remove ${itinerary.itemList[itemIndex].name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") {_, _ ->
                run {
                    removeItem(itemIndex)
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        dialog.setOnDismissListener {
            if(!isNew && itinerary.itemList.isEmpty()) {
                val emptyItineraryDialog = AlertDialog.Builder(requireContext())
                    .setCancelable(true)
                    .setTitle("Empty Itinerary")
                    .setMessage("You have removed all locations. Delete itinerary?")
                    .setPositiveButton("Yes") { _, _ -> deleteItinerary() }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    .create()
                emptyItineraryDialog.show()
            }
        }

        dialog.show()
    }

    private fun removeItem(itemIndex: Int) {
        itinerary.itemList.removeAt(itemIndex)
        rvItinerary.adapter?.notifyDataSetChanged()
        if(itinerary.itemList.isEmpty()) {
            tvTapAdd.visibility = View.VISIBLE
            tvItineraryDuration.text = "Travel time: 0 hours, 0 minutes"
        }
        else {
            calculateDuration()
        }
    }

    private fun calculateDuration() {
        val userLocation = (activity as MainActivity).getLastLocation()
        if((itinerary.itemList.size == 1 && userLocation != null) ||
            itinerary.itemList.size > 1) {
            EspressoIdlingResource.increment()
            durationLoadingDialog.show()
            viewModel.setDurationParams(itinerary, userLocation,
                resources.getString(R.string.google_api_key))
        }
        else if(itinerary.itemList.size == 1 && userLocation == null) {
            tvItineraryDuration.text = "Travel time: 0 hours, 0 minutes"
        }
    }

    private fun deleteItinerary() {
        viewModel.deleteItinerary(itinerary.dbId)
        goBack(true)
    }
}
