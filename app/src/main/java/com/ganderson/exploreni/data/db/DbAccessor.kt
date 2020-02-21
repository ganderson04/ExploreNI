package com.ganderson.exploreni.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.couchbase.lite.*
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.toDataClass
import com.ganderson.exploreni.toHashMap

class DbAccessor {
    companion object {
        private val database = DbManager.getSharedInstance().database

        fun getFavouriteLocations() : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type").equalTo(Expression.string("location")))

            query.addChangeListener {
                val results = it.results
                val locationList = ArrayList<NiLocation>()

                results.forEach { result ->
                    val valueMap = result.getDictionary(database.name).toMap()

                    // The document type is not part of the data class so it is removed from the
                    // map before conversion.
                    valueMap.remove("type")

                    val niLocation: NiLocation = valueMap.toDataClass()
                    locationList.add(niLocation)
                }

                data.value = locationList
            }

            query.execute()
            return data
        }

        fun addFavouriteLocation(niLocation: NiLocation): Boolean {
            val locationMap = niLocation.toHashMap()

            // Couchbase Lite does not store documents in separate collections so a "type" property
            // is assigned here to differentiate documents.
            locationMap["type"] = "location"

            val document = MutableDocument(locationMap)
            database.save(document)
            return true
        }

        fun removeFavouriteLocation(locationId: String): Boolean {
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("id").equalTo(Expression.string(locationId)))
            val resultSet = query.execute()
            val result = resultSet.next()
            val docId = result.getString("id")
            val document = database.getDocument(docId)
            database.delete(document)
            return true
        }

        fun isFavouriteLocation(locationId: String): Boolean {
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("id").equalTo(Expression.string(locationId)))
            val resultSet = query.execute()
            return resultSet.next() != null
        }
    }
}