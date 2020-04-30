package com.ganderson.exploreni.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ganderson.exploreni.data.ExploreRepository

class InterestsViewModel : ViewModel() {
    val interests = ExploreRepository.getInterests()

    fun setInterests(interests: List<String>) : Boolean {
        return ExploreRepository.setInterests(interests)
    }
}