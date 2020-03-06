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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.ganderson.exploreni.R
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_itinerary_viewer.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
const val ADD_ITEM_CODE = 1
class ItineraryViewerFragment(val isNew: Boolean) : Fragment() {
    private var name = "New Itinerary"
    private val itemList = ArrayList<NiLocation>()

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
        if(itemList.isNotEmpty()) {
            tvTapAdd.visibility = View.GONE
            rvItinerary.visibility = View.VISIBLE
        }

        val itemTouchHelper = ItemTouchHelper(ItemTouchCallback(itemList))
        itemTouchHelper.attachToRecyclerView(rvItinerary)

        val removeListener = object: ItineraryAdapter.OnRemoveClickListener {
            override fun onRemoveClick(item: NiLocation, itemIndex: Int) {
                confirmItemRemoval(item, itemIndex)
            }
        }
        rvItinerary.layoutManager = LinearLayoutManager(requireContext())
        rvItinerary.adapter = ItineraryAdapter(requireContext(), itemList, removeListener)
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
                    this.name = name
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

    override fun onResume() {
        super.onResume()
        tvItineraryName.text = name
        rvItinerary.adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val mainActivity = activity as MainActivity
                if(isNew) {
                    mainActivity.displayFragment(PlanFragment())
                }
                else parentFragmentManager.popBackStack()
            }
            R.id.tb_add_location -> goToExplore()
            R.id.tb_edit_itinerary -> Toast
                .makeText(requireContext(), "Edit", Toast.LENGTH_SHORT).show()
            R.id.tb_itinerary_map -> Toast
                .makeText(requireContext(), "Map", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToExplore() {
        val exploreFragment = ExploreFragment()
        val mainActivity = activity as MainActivity

        exploreFragment.setTargetFragment(this, ADD_ITEM_CODE)
        mainActivity.displayFragment(exploreFragment)
    }

    fun addItem(item: NiLocation) {
        itemList.add(item)
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
        dialog.show()
    }

    private fun removeItem(itemIndex: Int) {
        itemList.removeAt(itemIndex)
        rvItinerary.adapter?.notifyDataSetChanged()
        if(itemList.isEmpty()) tvTapAdd.visibility = View.VISIBLE
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
