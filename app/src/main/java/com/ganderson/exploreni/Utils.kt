package com.ganderson.exploreni

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat
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
         * Adapted from: https://rosettacode.org/wiki/Haversine_formula#Kotlin
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
    }
}

fun <T> T.toHashMap() : HashMap<String, Any> {
    return convert()
}

inline fun <reified T> Map<String, Any>.toDataClass() : T {
    return convert()
}

inline fun <S, reified T> S.convert() : T {
    val gson = Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, object: TypeToken<T>(){}.type)
}