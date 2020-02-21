package com.ganderson.exploreni.data.db

import android.content.Context
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration

class DbManager private constructor(context: Context) {
    companion object {
        private const val DB_NAME = "eni_db"
        private var instance: DbManager? = null

        fun getSharedInstance(context: Context) : DbManager {
            if(instance == null) {
                instance = DbManager(context)
            }
            return instance!!
        }
    }

    var database: Database

    init {
        val config = DatabaseConfiguration(context)
        val dir = context.getDir("CBL", Context.MODE_PRIVATE)
        config.setDirectory(dir.toString())
        database = Database(DB_NAME, config)
    }
}