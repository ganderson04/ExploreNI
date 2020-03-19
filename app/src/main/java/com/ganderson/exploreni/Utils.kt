package com.ganderson.exploreni

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * Contains utility methods for use throughout the application.
 */
class Utils {

    // Kotlin does not support classes of static methods, or "library classes", in the same way
    // Java does. "Companion objects" can be used to provide this functionality.
    companion object {
        private const val EARTH_RADIUS = 6371.0 // Earth's radius in kilometres
        private const val DISTANCE_SCALE = 1.609 // Conversion between miles and kilometres
        val DISTANCE_FORMATTER = DecimalFormat("0.00")

        /**
         * Calculates the Great Circle distance between locations using the Haversine
         * formula.
         * Adapted from: https://rosettacode.org/wiki/Haversine_formula#Java
         */
        fun getHaversineGCD(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double {
            val lat1Radians = Math.toRadians(lat1)
            val lat2Radians = Math.toRadians(lat2)
            val latDiff = Math.toRadians(lat2 - lat1)
            val lonDiff = Math.toRadians(lon2 - lon1)

            val a = sin(latDiff / 2).pow(2) +
                    sin(lonDiff / 2).pow(2) *
                    cos(lat1Radians) *
                    cos(lat2Radians)
            return 2.0 * EARTH_RADIUS * asin(sqrt(a))
        }

        fun distanceToImperial(metricDistance: Double) = metricDistance / DISTANCE_SCALE

        fun distanceToMetric(imperialDistance: Double) = imperialDistance * DISTANCE_SCALE

        fun secondsToTimeString(seconds: Int) : String {
            val hours = seconds / 3600
            val remainder = hours % 3600
            val minutes = remainder / 60

            var str: String = ""
            if(hours > 0) {
                if (hours > 1) str += "hours"
                else str += "hour"
            }

            if(minutes > 0) {
                if(minutes > 1) str += ", $minutes minutes"
                else str += ", $minutes minute"
            }

            return str
        }
    }
}

// Extension functions which become part of any object, even those in the SDK. These are used
// specifically to convert data class objects to and from Maps when storing them in and retrieving
// them from the Couchbase database.
// These functions are adapted from Tom Hanley's answer on StackOverflow:
// https://stackoverflow.com/a/56347214/8100469
fun <T> T.toHashMap() : HashMap<String, Any> {
    return convert()
}

inline fun <reified T> Map<String, Any>.toDataClass() : T {
    return convert()
}

// Converting between data class and Map utilises Gson's ability to convert objects to and from
// JSON.
inline fun <S, reified T> S.convert() : T {
    val gson = Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, object: TypeToken<T>(){}.type)
}