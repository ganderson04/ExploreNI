package com.ganderson.exploreni.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.CouchbaseLite
import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.viewmodels.InterestsViewModel

import kotlinx.android.synthetic.main.activity_interest.*

class InterestActivity : AppCompatActivity() {
    private val viewModel = InterestsViewModel()
    private val interests = ArrayList<String>()
    private val selectedInterests = ArrayList<String>()
    private lateinit var adapter: InterestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Interests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        CouchbaseLite.init(this)

        interests.addAll(resources.getStringArray(R.array.interests))
        rvInterests.layoutManager = LinearLayoutManager(this)
        adapter = InterestAdapter(this,
            resources.getStringArray(R.array.interests))
        rvInterests.adapter = adapter
        getUserInterests()
    }

    private fun getUserInterests() {
        val userInterests = viewModel.getInterests()
        userInterests?.let { list ->
            selectedInterests.addAll(list)
            selectedInterests.forEach { interest ->
                val itemIndex = interests.indexOf(interest)
                if(itemIndex != -1) {
                    adapter.setItemChecked(itemIndex)
                }
            }
        }
    }

    private fun addInterest(interest: String) {
        selectedInterests.add(interest)
    }

    private fun removeInterest(interest: String) {
        selectedInterests.remove(interest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                viewModel.setInterests(selectedInterests)
                this.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class InterestAdapter(private val context: Context, private val interests: Array<String>)
        : RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

        private val interestActivity = context as InterestActivity
        private val itemCheckStates = SparseBooleanArray()

        class InterestViewHolder(val view: View) : RecyclerView.ViewHolder(view){
            val tvInterest = view.findViewById<TextView>(R.id.tvInterest)
            val cbInterest = view.findViewById<CheckBox>(R.id.cbInterest)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_interest_item, parent, false)
            return InterestViewHolder(view)
        }

        override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
            val interest = interests[position]
            holder.tvInterest.text = interest

            val cbInterest = holder.cbInterest
            cbInterest.isChecked = itemCheckStates[position, false]
            cbInterest.setOnCheckedChangeListener { _, isChecked ->
                itemCheckStates.put(position, isChecked)
                if(isChecked) interestActivity.addInterest(interest)
                else interestActivity.removeInterest(interest)
            }
        }

        override fun getItemCount() = interests.size

        fun setItemChecked(position: Int) {
            itemCheckStates.put(position, true)
        }
    }
}
