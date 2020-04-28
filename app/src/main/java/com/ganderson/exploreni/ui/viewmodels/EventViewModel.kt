package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.Event

class EventViewModel : ViewModel() {
    fun getEvents() : LiveData<DataResult<List<Event>>> {
        return ExploreRepository.getEvents()
    }
}