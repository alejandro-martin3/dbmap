package com.alejandromartin.dbmap

object ZoneManager {

    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    data class GeohashBounds(
        val latMin: Double,
        val latMax: Double,
        val lonMin: Double,
        val lonMax: Double
    )

    fun toReducedGeohash(latitude: Double, longitude: Double, precision: Int = 6): String {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0

        var isEven = true
        var bit = 0
        var character = 0
        val geohash = StringBuilder()

        while (geohash.length < precision) {
            if (isEven) {
                val mid = (lonMin + lonMax) / 2
                if (longitude >= mid) {
                    character = character * 2 + 1
                    lonMin = mid
                } else {
                    character *= 2
                    lonMax = mid
                }
            } else {
                val mid = (latMin + latMax) / 2
                if (latitude >= mid) {
                    character = character * 2 + 1
                    latMin = mid
                } else {
                    character *= 2
                    latMax = mid
                }
            }

            isEven = !isEven

            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[character])
                bit = 0
                character = 0
            }
        }

        return geohash.toString()
    }

    fun getBounds(geohash: String): GeohashBounds {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        var isEven = true

        geohash.forEach { char ->
            val value = BASE32.indexOf(char)
            if (value == -1) return@forEach

            for (mask in listOf(16, 8, 4, 2, 1)) {
                if (isEven) {
                    val mid = (lonMin + lonMax) / 2
                    if ((value and mask) != 0) {
                        lonMin = mid
                    } else {
                        lonMax = mid
                    }
                } else {
                    val mid = (latMin + latMax) / 2
                    if ((value and mask) != 0) {
                        latMin = mid
                    } else {
                        latMax = mid
                    }
                }

                isEven = !isEven
            }
        }

        return GeohashBounds(
            latMin = latMin,
            latMax = latMax,
            lonMin = lonMin,
            lonMax = lonMax
        )
    }

    fun getApproximateCenter(geohash: String): Pair<Double, Double> {
        val bounds = getBounds(geohash)

        val centroLatitud = (bounds.latMin + bounds.latMax) / 2
        val centroLongitud = (bounds.lonMin + bounds.lonMax) / 2

        return Pair(centroLatitud, centroLongitud)
    }
}