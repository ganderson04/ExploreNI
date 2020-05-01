package com.ganderson.exploreni.entities.data.api

/**
 * Represents an attraction in Northern Ireland.
 */
data class NiLocation(val id: String,
                      val name: String,
                      val elevation: Float,
                      val town: String,
                      val lat: String,
                      val long: String,
                      val desc: String,
                      val imgUrl: String,
                      val imgAttr: String,
                      val website: String,
                      val locTags: List<String>)