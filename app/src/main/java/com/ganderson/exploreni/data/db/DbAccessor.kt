package com.ganderson.exploreni.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.couchbase.lite.*
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.toDataClass
import com.ganderson.exploreni.toHashMap
import java.util.*
import kotlin.collections.ArrayList

class DbAccessor {
    companion object {
        private val database = DbManager.getSharedInstance().database

        fun getFavouriteLocations() : LiveData<List<NiLocation>> {
            val data = MutableLiveData<List<NiLocation>>()
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("location")))

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

        fun saveItinerary(itinerary: Itinerary) : Boolean {
            val itineraryMap = itinerary.toHashMap()
            val document: MutableDocument?

            // Check if itinerary exists and update it if so.
            if(itinerary.dbId.isNotBlank()) {
                document = database.getDocument(itinerary.dbId).toMutable()
                document.setData(itineraryMap)
            }
            else {
                document = MutableDocument(itineraryMap)
            }

            document.setString("type", "itinerary")
            // Itinerary names are also stored in lowercase to be used in checking for duplicate
            // names.
            document.setString("unique_name", itinerary.name.toLowerCase(Locale.getDefault()))

            database.save(document)
            return true
        }

        fun isDuplicateItineraryName(name: String) : Boolean {
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("unique_name")
                    .equalTo(Expression.string(name.toLowerCase(Locale.getDefault()))))
            val resultSet = query.execute()
            return resultSet.next() != null
        }

        fun getItineraries() : LiveData<List<Itinerary>> {
            val data = MutableLiveData<List<Itinerary>>()
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("itinerary")))

            query.addChangeListener {
                val results = it.results
                val itineraryList = ArrayList<Itinerary>()

                results.forEach { result ->
                    val valueMap = result.getDictionary(database.name).toMap()
                    valueMap["dbId"] = result.getValue("id")

                    // type and unique_name are not part of the data class so they are removed from
                    // the map before conversion.
                    valueMap.remove("type")
                    valueMap.remove("unique_name")

                    val itinerary: Itinerary = valueMap.toDataClass()
                    itineraryList.add(itinerary)
                }

                data.value = itineraryList
            }

            query.execute()
            return data
        }

        fun deleteItinerary(dbId: String): Boolean {
            val document = database.getDocument(dbId)
            database.delete(document)
            return true
        }

        fun setInterests(interests: List<String>) : Boolean {
            val document: MutableDocument
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("interests")))
            val resultSet = query.execute()
            val result = resultSet.next()
            if(result != null) {
                document = database.getDocument(result.getString("id")).toMutable()
                document.keys
                document.setValue("interests", interests)
            }
            else {
                val interestMap = HashMap<String, Any>()
                interestMap["interests"] = interests
                interestMap["type"] = "interests"
                document = MutableDocument(interestMap)
            }
            database.save(document)
            return true
        }

        fun getInterests() : List<String> {
            val interests = ArrayList<String>()
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("interests")))

            val resultSet = query.execute()
            val result = resultSet.next()
            val interestMap: Map<String, Any>? = result?.getDictionary(database.name)?.toMap()
            interestMap?.let {
                interests.addAll(interestMap["interests"] as List<String>)
            }
            return interests
        }
    }
}