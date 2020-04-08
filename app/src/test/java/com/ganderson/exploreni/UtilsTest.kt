package com.ganderson.exploreni

import com.google.gson.JsonParseException
import org.jetbrains.annotations.NotNull
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertFailsWith

class UtilsTest {

    @Test
    fun getHaversineGCDTest() {
        // Belfast City Hall
        val lat1 = 54.597181
        val long1 = -5.930148

        // Belfast Castle
        val lat2 = 54.6427654
        val long2 = -5.942225

        // The distance with which to compare the one calculated using the Utils method was
        // calculated using https://www.movable-type.co.uk/scripts/latlong.html beforehand and the
        // result (5.128km) rounded up to 5.13.
        val expectedDistance = 5.13
        val distance = Utils.getHaversineGCD(lat1, long1, lat2, long2)

        // The distance is turned into a BigDecimal object in order to set the rounding.
        val distanceAsBigDecimal = BigDecimal(distance).setScale(2, RoundingMode.HALF_UP)
        assert(distanceAsBigDecimal.toDouble() == expectedDistance)
    }

    @Test
    fun distanceToImperialTest() {
        val metricDistance = 5.0
        val expectedImperialDistance = 3.11
        val imperialDistance = Utils.distanceToImperial(metricDistance)
        val imperialDistanceAsBigDecimal = BigDecimal(imperialDistance)
            .setScale(2, RoundingMode.HALF_UP)
        assert(imperialDistanceAsBigDecimal.toDouble() == expectedImperialDistance)
    }

    @Test
    fun distanceToMetricTest() {
        val imperialDistance = 3.11
        val expectedMetricDistance = 5.0
        val metricDistance = Utils.distanceToMetric(imperialDistance)
        val metricDistanceAsBigDecimal = BigDecimal(metricDistance)
            .setScale(2, RoundingMode.HALF_UP)
        assert(metricDistanceAsBigDecimal.toDouble() == expectedMetricDistance)
    }

    @Test
    fun secondsToTimeStringTestMinutes() {
        val seconds = 60
        val expectedTimeString = "1 minute"
        val timeString = Utils.secondsToTimeString(seconds)

        // Although considered a no-no in Java, "==" is interpreted as ".equals()" on reference
        // types in Kotlin. "===" is used to check if two references point to the same object in
        // the same way Java uses "==" on reference types.
        // Ref: https://kotlinlang.org/docs/reference/equality.html
        assert(timeString == expectedTimeString)
    }

    @Test
    fun secondsToTimeStringHoursTest() {
        val seconds = 3600
        val expectedTimeString = "1 hour"
        val timeString = Utils.secondsToTimeString(seconds)
        assert(timeString == expectedTimeString)
    }

    @Test
    fun secondsToTimeStringHoursAndMinutesTest() {
        val seconds = 3660
        val expectedTimeString = "1 hour, 1 minute"
        val timeString = Utils.secondsToTimeString(seconds)
        assert(timeString == expectedTimeString)
    }

    @Test
    fun secondsToTimeStringEmptyTest() {
        val seconds = 59
        val timeString = Utils.secondsToTimeString(seconds)
        assert(timeString.isEmpty())
    }

    @Test
    fun toHashMapTest() {
        val utilsTestClass = UtilsTestClass("Hello", 1)
        val utilsTestClassHashMap = utilsTestClass.toHashMap()
        assert(utilsTestClassHashMap.contains("testStr") &&
                utilsTestClassHashMap.contains("testInt"))

        // Gson's conversion of integers to doubles has been encountered by other users such as
        // in this StackOverflow question:
        // https://stackoverflow.com/q/36508323/8100469
        // Whilst a solution was given in the question above, this behaviour cannot be controlled
        // here due to the generic nature of the Utils#convert method. It is necessary, then, to
        // compare the stored value to 1.0 instead of 1.
        assert(utilsTestClassHashMap["testStr"] == "Hello" &&
                utilsTestClassHashMap["testInt"] == 1.0)
    }

    @Test
    fun toDataClassTest() {
        val hashMap = HashMap<String, Any>().apply {
            put("testStr", "Hello")
            put("testInt", 1)
        }
        val dataClass: UtilsTestClass = hashMap.toDataClass()
        assert(dataClass::class.isData) // KClass#isData uses reflection.
    }

    @Test
    fun toDataClassFailureTest() {
        // Trying to assign a different type to "testInt" will cause a JsonParseException. Kotlin
        // treats non-nullable "Int" variables (i.e. those not declared as "Int?") as primitive
        // ints.
        // Interestingly, although "testStr" is not declared "String?" and therefore should be
        // non-nullable, it can still end up as null if the key is missing (it is not possible,
        // however, to set the value to null in the HashMap as the value type is not marked "Any?").
        // This is possible because Gson uses the "magic" sun.misc.Unsafe class which skips
        // initialisation and security checks. Ref: https://stackoverflow.com/a/45793703/8100469
        val hashMap = HashMap<String, Any>().apply {
            put("testStr", "Hello")
            put("testInt", "World")
        }

        assertFailsWith<JsonParseException> {
            val data = hashMap.toDataClass<UtilsTestClass>()
            println(data)
        }
    }

    data class UtilsTestClass(val testStr: String, val testInt: Int)
}