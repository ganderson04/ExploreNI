package com.ganderson.exploreni.entities.api

import java.time.LocalDate

data class Event(val id: String,
                 val name: String,
                 val desc: String,
                 val startDate: LocalDate,
                 val endDate: LocalDate,
                 val imgUrl: String,
                 val imgAttr: String,
                 val website: String)