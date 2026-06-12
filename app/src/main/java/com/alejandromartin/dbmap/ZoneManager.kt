package com.alejandromartin.dbmap

/**
 * Gestiona la conversión entre coordenadas geográficas y zonas geohash reducidas.
 * Se utiliza para obtener una zona aproximada a partir de la ubicación del usuario,
 * evitando guardar coordenadas exactas y protegiendo su privacidad.
 */
object ZoneManager {

    // Alfabeto base32 estándar utilizado en la codificación geohash
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Representa los límites geográficos de una celda geohash.
     *
     * @property latMin Latitud mínima del área.
     * @property latMax Latitud máxima del área.
     * @property lonMin Longitud mínima del área.
     * @property lonMax Longitud máxima del área.
     */
    data class GeohashBounds(
        val latMin: Double,
        val latMax: Double,
        val lonMin: Double,
        val lonMax: Double
    )

    /**
     * Convierte unas coordenadas geográficas en un geohash reducido de la precisión indicada.
     * A precisión 6, cada celda representa un área de aproximadamente 1.2 × 0.6 km.
     *
     * @param latitude Latitud de la ubicación.
     * @param longitude Longitud de la ubicación.
     * @param precision Número de caracteres del geohash resultante. Por defecto 6.
     * @return Geohash como cadena de texto en base32.
     */
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
                // Bits pares codifican la longitud
                val mid = (lonMin + lonMax) / 2
                if (longitude >= mid) {
                    character = character * 2 + 1
                    lonMin = mid
                } else {
                    character *= 2
                    lonMax = mid
                }
            } else {
                // Bits impares codifican la latitud
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
                // Cada 5 bits se genera un carácter base32
                geohash.append(BASE32[character])
                bit = 0
                character = 0
            }
        }

        return geohash.toString()
    }

    /**
     * Calcula los límites geográficos (bounding box) de una celda geohash.
     *
     * @param geohash Cadena geohash a decodificar.
     * @return GeohashBounds con las coordenadas mínimas y máximas del área.
     */
    fun getBounds(geohash: String): GeohashBounds {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        var isEven = true

        geohash.forEach { char ->
            val value = BASE32.indexOf(char)
            if (value == -1) return@forEach

            // Decodifica cada carácter bit a bit usando máscaras de 5 bits
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

    /**
     * Calcula el centro aproximado de una celda geohash como punto medio de sus límites.
     *
     * @param geohash Cadena geohash de la zona.
     * @return Par (latitud, longitud) del centro aproximado de la celda.
     */
    fun getApproximateCenter(geohash: String): Pair<Double, Double> {
        val bounds = getBounds(geohash)

        val centroLatitud = (bounds.latMin + bounds.latMax) / 2
        val centroLongitud = (bounds.lonMin + bounds.lonMax) / 2

        return Pair(centroLatitud, centroLongitud)
    }
}