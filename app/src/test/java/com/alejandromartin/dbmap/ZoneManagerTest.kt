package com.alejandromartin.dbmap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para ZoneManager.
 * Verifica la generación de geohashes, sus límites y el cálculo del centro aproximado.
 */
class ZoneManagerTest {

    /** Comprueba que el geohash generado tiene exactamente 6 caracteres de precisión. */
    @Test
    fun geohashTienePrecisionSeisPorDefecto() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        assertEquals(6, geohash.length)
    }

    /** Comprueba que la misma ubicación siempre produce el mismo geohash. */
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

    /** Comprueba que ubicaciones geográficamente distintas producen geohashes diferentes. */
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

    /** Comprueba que el geohash solo contiene caracteres del alfabeto base32 estándar. */
    @Test
    fun geohashSoloContieneCaracteresValidos() {
        val geohash = ZoneManager.toReducedGeohash(
            latitude = 41.4650,
            longitude = 2.2700
        )

        val caracteresValidos = "0123456789bcdefghjkmnpqrstuvwxyz"

        assertTrue(geohash.all { it in caracteresValidos })
    }

    /** Comprueba que la ubicación original queda dentro de los límites del geohash generado. */
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

    /** Comprueba que los límites del geohash son coherentes: mínimo siempre menor que máximo. */
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

    /** Comprueba que el centro calculado cae dentro de los límites del geohash. */
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

    /** Comprueba que el centro calculado corresponde exactamente al punto medio de los límites. */
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