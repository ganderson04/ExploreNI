package com.ganderson.exploreni.data.db

import com.couchbase.lite.*
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.toHashMap

class DbAccessor {
    companion object {
        private val database = DbManager.getSharedInstance().database


        fun addFavouriteLocation(niLocation: NiLocation): Boolean {
            val locationMap = niLocation.toHashMap()
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