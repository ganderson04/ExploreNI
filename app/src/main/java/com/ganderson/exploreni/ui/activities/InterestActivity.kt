package com.ganderson.exploreni.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ganderson.exploreni.R

import kotlinx.android.synthetic.main.activity_interest.*

class InterestActivity : AppCompatActivity() {
    private val selectedInterests = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Interests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvInterests.layoutManager = LinearLayoutManager(this)
        rvInterests.adapter = InterestAdapter(this,
            resources.getStringArray(R.array.interests))
    }

    fun addInterest(interest: String) {
        selectedInterests.add(interest)
    }

    fun removeInterest(interest: String) {
        selectedInterests.remove(interest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class InterestAdapter(private val context: Context, private val interests: Array<String>)
        : RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

        private val interestActivity = context as InterestActivity

        class InterestViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
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
            cbInterest.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) interestActivity.addInterest(interest)
                else interestActivity.removeInterest(interest)
            }
        }

        override fun getItemCount() = interests.size
    }
}
