package com.ganderson.exploreni.ui.components

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ganderson.exploreni.entities.data.api.NiLocation
import java.util.*

class ItineraryItemTouchCallback(private val itemList: List<NiLocation>,
                                 private val itemMovedCallback: ItemMovedCallback)
    : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN
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