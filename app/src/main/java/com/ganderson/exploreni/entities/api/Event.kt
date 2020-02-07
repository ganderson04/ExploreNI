package com.ganderson.exploreni.entities.api

import java.util.*

data class Event(val id: String,
                 val name: String,
                 val desc: String,
                 val startDate: Date,
                 val endDate: Date,
                 val imgUrl: String,
                 val imgAttr: String,
                 val website: String
                 )