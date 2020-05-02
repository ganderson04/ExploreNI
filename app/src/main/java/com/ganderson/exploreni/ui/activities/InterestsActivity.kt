package com.ganderson.exploreni.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.CouchbaseLite
import com.ganderson.exploreni.R
import com.ganderson.exploreni.ui.components.adapters.InterestsAdapter
import com.ganderson.exploreni.ui.viewmodels.InterestsViewModel

import kotlinx.android.synthetic.main.activity_interest.*

class InterestsActivity : AppCompatActivity() {
    private val viewModel: InterestsViewModel by viewModels()
    private val interests = ArrayList<String>()
    private val selectedInterests = ArrayList<String>()
    private lateinit var adapter: InterestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the title.
        supportActionBar?.title = "Interests"

        // Show the back button in the toolbar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Couchbase Lite must be initialised in each Activity that uses it.
        CouchbaseLite.init(this)

        interests.addAll(resources.getStringArray(R.array.interests))
        rvInterests.layoutManager = LinearLayoutManager(this)
        adapter = InterestsAdapter(this,
            resources.getStringArray(R.array.interests))
        rvInterests.adapter = adapter
        getUserInterests()
    }

    private fun getUserInterests() {
        val userInterests = viewModel.interests
        if(userInterests.isNotEmpty()) {
            selectedInterests.addAll(userInterests)
            selectedInterests.forEach { interest ->
                val itemIndex = interests.indexOf(interest)
                if(itemIndex != -1) {
                    adapter.setItemChecked(itemIndex)
                }
            }
        }
    }

    fun addInterest(interest: String) {
        selectedInterests.add(interest)
    }

    fun removeInterest(interest: String) {
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
}
