package com.ganderson.exploreni.entities

import com.ganderson.exploreni.entities.api.NiLocation

data class Itinerary(var dbId: String = "",
                     var name: String = "New Itinerary",
                     val itemList: ArrayList<NiLocation> = ArrayList())