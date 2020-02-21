package com.ganderson.exploreni.data.db

import android.content.Context
import com.couchbase.lite.*
import com.ganderson.exploreni.entities.api.NiLocation
import com.ganderson.exploreni.toHashMap

class DbAccessor(context: Context) {
    private val dbManager = DbManager.getSharedInstance(context)

    fun addFavouriteLocation(niLocation: NiLocation) : Boolean {
        val locationMap = niLocation.toHashMap()
        locationMap.put("type", "location")

        val document = MutableDocument(locationMap)
        dbManager.database.save(document)
        return true
    }

    fun removeFavouriteLocation(locationId: String): Boolean {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(dbManager.database))
            .where(Expression.property("id").equalTo(Expression.string(locationId)))
        val resultSet = query.execute()
        val result = resultSet.next()
        val docId = result.getString("id")
        val document = dbManager.database.getDocument(docId)
        dbManager.database.delete(document)
        return true
    }

    fun isFavouriteLocation(locationId: String) : Boolean {
        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(dbManager.database))
            .where(Expression.property("id").equalTo(Expression.string(locationId)))
        val resultSet = query.execute()
        return resultSet.next() != null
    }
}