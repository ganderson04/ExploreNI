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
import androidx.cardview.widget.CardView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

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

        val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(itinerary.itemList))
        itemTouchHelper.attachToRecyclerView(rvItinerary)

        val removeListener = object: ItineraryAdapter.OnRemoveClickListener {
            override fun onRemoveClick(item: NiLocation, itemIndex: Int) {
                confirmItemRemoval(item, itemIndex)
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
        if(itinerary.itemList.size > 1) {
            val mainActivity = activity as MainActivity
            mainActivity.displayFragment(ItineraryMapFragment(itinerary))
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

    private fun confirmItemRemoval(item: NiLocation, itemIndex: Int) {
        val dialog = AlertDialog.Builder(this.context)
            .setTitle("Confirm removal")
            .setMessage("Remove ${item.name}?")
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
            tvItineraryDuration.text = "0 hours, 0 minutes"
        }
        else {
            calculateDuration()
        }
    }

    private fun calculateDuration() {
        if(itinerary.itemList.size > 1) {
            val loadingDialog = LoadingDialog(
                requireContext(),
                "Calculating duration, please wait."
            )
            loadingDialog.show()
            viewModel
                .calculateDuration(itinerary, resources.getString(R.string.google_api_key))
                .observe(viewLifecycleOwner) { seconds ->
                    loadingDialog.dismiss()
                    val duration = Utils.secondsToTimeString(seconds)
                    tvItineraryDuration.text = duration
                }
        }
    }

    private fun deleteItinerary() {
        viewModel.deleteItinerary(itinerary.dbId)
        goBack(true)
    }

    class ItineraryAdapter(val context: Context, val itemList: List<NiLocation>,
                           val listener: OnRemoveClickListener)
        : RecyclerView.Adapter<ItineraryAdapter.ItemViewHolder>() {

        interface OnRemoveClickListener {
            fun onRemoveClick(item: NiLocation, itemIndex: Int)
        }

        class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val cvItineraryItem = view.findViewById<CardView>(R.id.cvItineraryItem)
            val tvItineraryItem = view.findViewById<TextView>(R.id.tvItineraryItem)
            val ivItineraryItem = view.findViewById<ImageView>(R.id.ivItineraryLocation)
            val ibRemoveItem = view.findViewById<ImageButton>(R.id.ibRemoveItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_itinerary_item,
                parent, false)
            return ItemViewHolder(view)
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
            holder.ibRemoveItem.setOnClickListener { listener.onRemoveClick(item, position)  }
        }
    }

    class ItemTouchCallback(val itemList: List<NiLocation>)
        : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN
            or ItemTouchHelper.START or ItemTouchHelper.END, 0) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            Collections.swap(itemList, fromPosition, toPosition)
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
}
