package com.ganderson.exploreni.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.couchbase.lite.*
import com.ganderson.exploreni.entities.Itinerary
import com.ganderson.exploreni.entities.data.DataResult
import com.ganderson.exploreni.entities.data.api.NiLocation
import com.ganderson.exploreni.toDataClass
import com.ganderson.exploreni.toHashMap
import java.util.*
import kotlin.collections.ArrayList

class DbAccessor {
    companion object {
        private val database = DbManager.getSharedInstance().database

        fun getFavouriteLocations() : LiveData<DataResult<List<NiLocation>>> {
            val data = MutableLiveData<DataResult<List<NiLocation>>>()
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("location")))

            // A listener is attached to update the LiveData each time a change is made to the
            // database that affects the query's result set, such as deletion.
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

                data.value = DataResult(locationList, null)
            }

            try {
                query.execute()
            }
            catch(e: Exception) {
                data.value = DataResult(null, e)
            }

            return data
        }

        fun addFavouriteLocation(niLocation: NiLocation): Boolean {
            val locationMap = niLocation.toHashMap()

            // Couchbase Lite does not store documents in separate collections so a "type" property
            // is assigned here to differentiate documents.
            locationMap["type"] = "location"

            val document = MutableDocument(locationMap)

            try {
                database.save(document)
                return true
            }
            catch(e: Exception) {
                return false
            }
        }

        fun removeFavouriteLocation(locationId: String): Boolean {
            // Meta.id retrieves the unique Couchbase Lite DB ID assigned to the document. It is
            // not included in "SelectResult.all()" used elsewhere in this class, and must always
            // be requested alongside "SelectResult.all()" if it is needed.
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("id").equalTo(Expression.string(locationId)))
            val resultSet = query.execute()
            val result = resultSet.next()
            val docId = result.getString("id")
            val document = database.getDocument(docId)

            try {
                database.delete(document)
                return true
            }
            catch (e: Exception) {
                return false
            }
        }

        fun isFavouriteLocation(locationId: String): Boolean {
            // "id" here is not the same as "Meta.id", rather it is the "id" field of the
            // NiLocation object which represents the assigned MongoDB document ID.
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("id").equalTo(Expression.string(locationId)))
            try {
                val resultSet = query.execute()
                return resultSet.next() != null
            }
            catch(e: Exception) {
                return false
            }
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

            try {
                database.save(document)
                return true
            }
            catch (e: Exception) {
                return false
            }
        }

        fun isDuplicateItineraryName(name: String) : Boolean {
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("unique_name")
                    .equalTo(Expression.string(name.toLowerCase(Locale.getDefault()))))
            try {
                val resultSet = query.execute()
                return resultSet.next() != null
            }
            catch(e: Exception) {
                return false
            }
        }

        /**
         * Convenience method for testing with a single itinerary.
         */
        fun getItinerary(itineraryName: String) : Itinerary {
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id), SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("unique_name")
                    .equalTo(Expression.string(itineraryName.toLowerCase(Locale.getDefault()))))
            val resultSet = query.execute()
            val result = resultSet.next()
            val itineraryMap = result.getDictionary(database.name).toMap()
            itineraryMap["dbId"] = result.getValue("id")

            // type and unique_name are not part of the data class so they are removed from
            // the map before conversion.
            itineraryMap.remove("type")
            itineraryMap.remove("unique_name")

            return itineraryMap.toDataClass()
        }

        fun getItineraries() : LiveData<DataResult<List<Itinerary>>> {
            val data = MutableLiveData<DataResult<List<Itinerary>>>()
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

                data.value = DataResult(itineraryList, null)
            }

            try {
                query.execute()
            }
            catch(e: Exception) {
                data.value = DataResult(null, e)
            }

            return data
        }

        fun deleteItinerary(dbId: String): Boolean {
            val document = database.getDocument(dbId)
            if(document != null) {
                try{
                    database.delete(document)
                }
                catch(e: Exception) {
                    return false
                }
            }
            return true
        }

        fun setInterests(interests: List<String>) : Boolean {
            val document: MutableDocument
            val query = QueryBuilder
                .select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("interests")))
            try {
                val resultSet = query.execute()
                val result = resultSet.next()

                // Update interests if the document already exists, otherwise create it.
                if(result != null) {
                    document = database.getDocument(result.getString("id")).toMutable()
                    document.setValue("interests", interests)
                }
                else {
                    val interestMap = HashMap<String, Any>()
                    interestMap["interests"] = interests
                    interestMap["type"] = "interests"
                    document = MutableDocument(interestMap)
                }
            }
            catch(e: Exception) {
                return false
            }

            database.save(document)
            return true
        }

        fun getInterests() : DataResult<List<String>> {
            val interests = ArrayList<String>()
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("type")
                    .equalTo(Expression.string("interests")))

            try {
                val resultSet = query.execute()
                val result = resultSet.next()

                // If there is a result, convert it to a Map, then add the interests to the above
                // list.
                val interestMap: Map<String, Any>? = result?.getDictionary(database.name)?.toMap()
                interestMap?.let {
                    interests.addAll(interestMap["interests"] as List<String>)
                }
            }
            catch(e: Exception) {
                return DataResult(emptyList(), e)
            }

            return DataResult(interests, null)
        }
    }
}