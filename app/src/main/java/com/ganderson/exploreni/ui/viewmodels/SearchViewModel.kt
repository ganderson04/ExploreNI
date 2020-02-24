package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository
import com.ganderson.exploreni.entities.api.NiLocation

class SearchViewModel : ViewModel() {
    fun performSearch(query: String) : LiveData<List<NiLocation>> {
        return ExploreRepository.performSearch(query)
    }
}