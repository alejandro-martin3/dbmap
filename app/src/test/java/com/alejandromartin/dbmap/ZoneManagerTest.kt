package com.alejandromartin.dbmap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoneManagerTest {

    @Test
    fun geohashTienePrecisionSeisPorDefecto() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        assertEquals(6, geohash.length)
    }

    @Test
    fun mismaUbicacionGeneraMismoGeohash() {
        val geohash1 = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val geohash2 = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        assertEquals(geohash1, geohash2)
    }

    @Test
    fun ubicacionOriginalEstaDentroDeSuGeohash() {
        val latitud = 41.4650
        val longitud = 2.2700

        val geohash = ZoneManager.toReducedGeohash(
            latitude = latitud,
            longitude = longitud
        )

        val bounds = ZoneManager.getBounds(geohash)

        assertTrue(latitud >= bounds.latMin)
        assertTrue(latitud <= bounds.latMax)
        assertTrue(longitud >= bounds.lonMin)
        assertTrue(longitud <= bounds.lonMax)
    }

    @Test
    fun centroCalculadoEstaDentroDeLosLimites() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val bounds = ZoneManager.getBounds(geohash)
        val centro = ZoneManager.getApproximateCenter(geohash)

        assertTrue(centro.first >= bounds.latMin)
        assertTrue(centro.first <= bounds.latMax)
        assertTrue(centro.second >= bounds.lonMin)
        assertTrue(centro.second <= bounds.lonMax)
    }
}