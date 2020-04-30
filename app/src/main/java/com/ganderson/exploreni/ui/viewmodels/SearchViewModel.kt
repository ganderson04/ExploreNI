package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.ganderson.exploreni.data.ExploreRepository

class SearchViewModel : ViewModel() {
    private val queryLiveData = MutableLiveData<String>()
    val searchResults = queryLiveData.switchMap { query ->
        ExploreRepository.performSearch(query)
    }

    fun performSearch(query: String) {
        queryLiveData.value = query
    }
}