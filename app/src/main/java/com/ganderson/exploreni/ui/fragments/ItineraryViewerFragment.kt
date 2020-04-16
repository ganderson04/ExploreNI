package com.ganderson.exploreni.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ganderson.exploreni.EspressoIdlingResource

import com.ganderson.exploreni.R
import com.ganderson.exploreni.Utils
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import com.ganderson.exploreni.ui.components.LoadingDialog
import com.ganderson.exploreni.ui.viewmodels.ItineraryViewerViewModel
import kotlinx.android.synthetic.main.fragment_itinerary_viewer.*
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
const val ADD_ITEM_CODE = 1
class ItineraryViewerFragment(val isNew: Boolean, savedItinerary: Itinerary?) : Fragment() {
    private val viewModel = ItineraryViewerViewModel()
    private val itinerary = savedItinerary ?: Itinerary()

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
        tvItineraryName.setOnClickListener { showInputDialog() }
        if(itinerary.itemList.isNotEmpty()) {
            tvTapAdd.visibility = View.GONE
            rvItinerary.visibility = View.VISIBLE
            calculateDuration()
        }

        val itemMovedCallback = object: ItemTouchCallback.ItemMovedCallback {
            override fun itemMoved() {
                calculateDuration()
            }
        }
        val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(itinerary.itemList,
            itemMovedCallback))
        itemTouchHelper.attachToRecyclerView(rvItinerary)

        val removeListener = object: ItineraryAdapter.OnRemoveClickListener {
            override fun onRemoveClick(itemIndex: Int) {
                confirmItemRemoval(itemIndex)
            }
        }
        rvItinerary.layoutManager = LinearLayoutManager(requireContext())
        rvItinerary.adapter = ItineraryAdapter(requireContext(), itinerary.itemList, removeListener)
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
        if(!name.equals(itinerary.name)) {
            if (viewModel.isDuplicateItineraryName(name)) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setCancelable(true)
                    .setTitle("Error")
                    .setMessage("That itinerary name already exists.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                dialog.show()
            } else {
                itinerary.name = name
                tvItineraryName.text = name
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
        if((isNew && itinerary.itemList.isNotEmpty()) || (!isNew && !wasDeleted)) {
            if (viewModel.saveItinerary(itinerary)) {
                Toast.makeText(requireContext(), "Itinerary saved.", Toast.LENGTH_SHORT).show()
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
                    .setPositiveButton("Yes") { dialog, _ -> deleteItinerary() }
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
        val userLocation = getUserLocation()
        if((itinerary.itemList.size == 1 && userLocation != null) ||
            itinerary.itemList.size > 1) {
            EspressoIdlingResource.increment()
            val loadingDialog = LoadingDialog(
                requireContext(),
                "Calculating duration, please wait."
            )
            loadingDialog.show()
            viewModel
                .calculateDuration(itinerary, userLocation, resources.getString(R.string.google_api_key))
                .observe(viewLifecycleOwner) { seconds ->
                    loadingDialog.dismiss()
                    val duration = Utils.secondsToTimeString(seconds)
                    val durationText = "Travel time: $duration"
                    tvItineraryDuration.text = durationText
                    EspressoIdlingResource.decrement()
                }
        }
    }

    private fun deleteItinerary() {
        viewModel.deleteItinerary(itinerary.dbId)
        goBack(true)
    }

    private fun getUserLocation() : Location? {
        var location: Location? = null
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager: LocationManager? = (activity as MainActivity)
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // If the LocationManager has been instantiated, check for providers. Kotlin "?"
            // performs a null check and ".let" runs the code inside the block if the object
            // under consideration is not null.
            locationManager?.let {
                // "it" refers to locationManager. "let" blocks are similar to lambdas.
                if(it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else if(it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = it.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
            }
        }
        return location
    }

    class ItineraryAdapter(val context: Context, private val itemList: List<NiLocation>,
                           private val removeListener: OnRemoveClickListener)
        : RecyclerView.Adapter<ItineraryAdapter.ItemViewHolder>() {

        interface OnRemoveClickListener {
            fun onRemoveClick(itemIndex: Int)
        }

        class ItemViewHolder(val view: View, val removeListener: OnRemoveClickListener)
            : RecyclerView.ViewHolder(view) {
            val tvItineraryItem = view.findViewById<TextView>(R.id.tvItineraryItem)
            val ivItineraryItem = view.findViewById<ImageView>(R.id.ivItineraryLocation)
            val ibRemoveItem = view.findViewById<ImageButton>(R.id.ibRemoveItem)

            init {
                ibRemoveItem.setOnClickListener { removeListener.onRemoveClick(adapterPosition) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_itinerary_item,
                parent, false)
            return ItemViewHolder(view, removeListener)
        }

        override fun getItemCount() = itemList.size

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = itemList[position]

            Glide.with(context)
                .load(item.imgUrl)
                .centerCrop()
                .error(R.drawable.placeholder_no_image_available)
                .into(holder.ivItineraryItem)

            holder.tvItineraryItem.text = item.name
        }
    }

    class ItemTouchCallback(private val itemList: List<NiLocation>,
                            private val itemMovedCallback: ItemMovedCallback)
        : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN
            or ItemTouchHelper.START or ItemTouchHelper.END, 0) {

        interface ItemMovedCallback {
            fun itemMoved()
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            Collections.swap(itemList, fromPosition, toPosition)
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            itemMovedCallback.itemMoved()
        }
    }
}
