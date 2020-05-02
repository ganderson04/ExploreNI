package com.ganderson.exploreni.ui.components.adapters

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.activities.InterestsActivity

class InterestsAdapter(private val context: Context, private val interests: Array<String>)
    : RecyclerView.Adapter<InterestsAdapter.InterestsViewHolder>() {
    private val interestsActivity = context as InterestsActivity

    // A SparseBooleanArray is used to monitor the checkbox state of each interest item.
    // A sparse array's indices can contain gaps and so, default values must be given when accessing
    // its items.
    private val itemCheckStates = SparseBooleanArray()

    class InterestsViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val tvInterest = view.findViewById<TextView>(R.id.tvInterest)
        val cbInterest = view.findViewById<CheckBox>(R.id.cbInterest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestsViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_interest_item, parent, false)
        return InterestsViewHolder(view)
    }

    override fun onBindViewHolder(holder: InterestsViewHolder, position: Int) {
        val interest = interests[position]
        holder.tvInterest.text = interest

        val cbInterest = holder.cbInterest
        cbInterest.isChecked = itemCheckStates[position, false] // [array position, default value]
        cbInterest.setOnCheckedChangeListener { _, isChecked ->
            itemCheckStates.put(position, isChecked)
            if(isChecked) interestsActivity.addInterest(interest)
            else interestsActivity.removeInterest(interest)
        }
    }

    override fun getItemCount() = interests.size

    fun setItemChecked(position: Int) {
        itemCheckStates.put(position, true)
    }
}