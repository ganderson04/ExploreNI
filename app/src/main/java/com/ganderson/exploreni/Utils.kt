package com.ganderson.exploreni

import kotlin.math.*

class Utils {

    companion object {
        private const val EARTH_RADIUS = 6372.8 // Earth's radius in kilometres

        fun getStraightLineDistance(x: Float, y: Float, z: Float) : Float {
            return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
        }

        fun getHaversineGCD(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double {
            val lat1Radians = Math.toRadians(lat1)
            val lat2Radians = Math.toRadians(lat2)
            val latDiff = Math.toRadians(lat2 - lat1)
            val lonDiff = Math.toRadians(lon2 - lon1)

            val a = sin(latDiff / 2).pow(2) +
                    sin(lonDiff / 2).pow(2) *
                    cos(lat1Radians) *
                    cos(lat2Radians)
            return 2.0 * asin(sqrt(a))
        }
    }
}