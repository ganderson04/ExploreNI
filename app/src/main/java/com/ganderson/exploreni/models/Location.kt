package com.ganderson.exploreni.models

/**
 * Represents an attraction in Northern Ireland.
 */
data class Location(val id: String,
                    val name: String,
                    val elevation: Float,
                    val town: String,
                    val lat: String,
                    val long: String,
                    val desc: String,
                    val imgUrl: String,
                    val imgAttr: String)