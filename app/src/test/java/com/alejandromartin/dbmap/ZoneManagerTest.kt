package com.alejandromartin.dbmap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun ubicacionesDistintasGeneranGeohashesDistintos() {
        val geohashBarcelona = ZoneManager.toReducedGeohash(
            latitude = 41.3874,
            longitude = 2.1686
        )

        val geohashMadrid = ZoneManager.toReducedGeohash(
            latitude = 40.4168,
            longitude = -3.7038
        )

        assertNotEquals(geohashBarcelona, geohashMadrid)
    }

    @Test
    fun geohashSoloContieneCaracteresValidos() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val caracteresValidos = "0123456789bcdefghjkmnpqrstuvwxyz"

        assertTrue(geohash.all { it in caracteresValidos })
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
    fun limitesDelGeohashSonCoherentes() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val bounds = ZoneManager.getBounds(geohash)

        assertTrue(bounds.latMin < bounds.latMax)
        assertTrue(bounds.lonMin < bounds.lonMax)
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

    @Test
    fun centroCalculadoCorrespondeAlPuntoMedioDeLosLimites() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val bounds = ZoneManager.getBounds(geohash)
        val centro = ZoneManager.getApproximateCenter(geohash)

        val latitudEsperada = (bounds.latMin + bounds.latMax) / 2
        val longitudEsperada = (bounds.lonMin + bounds.lonMax) / 2

        assertEquals(latitudEsperada, centro.first, 0.000001)
        assertEquals(longitudEsperada, centro.second, 0.000001)
    }
}