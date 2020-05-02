package com.ganderson.exploreni.data.db

import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration

class DbManager private constructor() {
    companion object {
        private const val DB_NAME = "eni_db"
        private var instance: DbManager? = null

        // Use of singleton for DB access.
        fun getSharedInstance() : DbManager {
            if(instance == null) {
                instance = DbManager()
            }
            return instance!!
        }
    }

    var database: Database

    init {
        val config = DatabaseConfiguration()
        database = Database(DB_NAME, config)
    }
}