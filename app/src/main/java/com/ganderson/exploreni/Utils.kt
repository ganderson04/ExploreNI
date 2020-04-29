package com.ganderson.exploreni

import androidx.test.espresso.idling.CountingIdlingResource
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
        const val MAX_SEEK_MILES = 30
        const val MAX_SEEK_KM = 50
        val distanceFormatter = DecimalFormat("0.00")

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
            val remainder = seconds - (hours * 3600)
            val minutes = remainder / 60

            var str = ""
            if(hours > 0) {
                if (hours > 1) str += "$hours hours"
                else str += "$hours hour"
            }

            if(minutes > 0) {
                if(hours > 0) str +=", "
                if(minutes > 1) str += "$minutes minutes"
                else str += "$minutes minute"
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

// Reified types can be accessed within the function body without needing to pass Class<T> (by, for
// example, T::class.java). Reified types can only be used in "inline" functions, however.
// "Inline" injects the code of a function at the call-site and avoids allocating an object for the
// function. Useful on functions taking a lambda as a parameter, but necessary here.
// Ref: https://kotlinlang.org/docs/reference/inline-functions.html
inline fun <reified T> Map<String, Any>.toDataClass() : T {
    return convert()
}

// Converting between data class and Map utilises Gson's ability to convert objects to and from
// JSON. The Gson library comes with Retrofit, although it does exist separately.
inline fun <S, reified T> S.convert() : T {
    val gson = Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, object: TypeToken<T>(){}.type)
}

// The below object is a singleton which provides access to Espresso's CountingIdlingResource.
// IdlingResources are used to instruct the Espresso test to wait for an asynchronous activity to
// finish and then proceed with the test instructions. Otherwise, the test would fail as the UI
// element(s) under consideration would not yet be ready.
//
// Kotlin allows singletons to be created by declaring them using the "object" keyword. The result
// looks like a class declaration and accessing its methods looks like accessing a static method in
// a class. For example, accessing the increment() method below would look like:
// EspressoIdlingResource.increment()
// Ref: https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations
object EspressoIdlingResource {
    private const val RESOURCE = "GLOBAL"

    // @JvmField means this property will be exposed as a field without getter/setter.
    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    // CountingIdlingResources work similar to semaphores. If their internal counter is not zero,
    // the app is not idle which means there is some asynchronous activity going on. This means
    // Espresso will wait.
    fun increment() = countingIdlingResource.increment()

    // If the CountingIdlingResource's counter is zero, it means the app is idle and Espresso can
    // continue with testing.
    fun decrement() {
        // Do not decrement counter if the app is already idle.
        if(!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}