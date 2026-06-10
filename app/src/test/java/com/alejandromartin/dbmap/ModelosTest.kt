package com.alejandromartin.dbmap

import com.alejandromartin.dbmap.model.Medicion
import com.alejandromartin.dbmap.model.Zona
import com.alejandromartin.dbmap.model.AgregadoZona

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelosTest {

    @Test
    fun medicionPorDefectoTieneValoresEsperados() {
        val medicion = Medicion()

        assertEquals("", medicion.medicionId)
        assertEquals("", medicion.zonaId)
        assertNull(medicion.agregadoId)
        assertEquals(0.0, medicion.nivelRuido, 0.0)
        assertTrue(medicion.fechaHora > 0L)
    }

    @Test
    fun medicionConValoresMantieneLaInformacion() {
        val medicion = Medicion(
            medicionId = "medicion_1",
            zonaId = "sp3e3x",
            agregadoId = "agregado_1",
            nivelRuido = 62.5,
            fechaHora = 1718000000000L
        )

        assertEquals("medicion_1", medicion.medicionId)
        assertEquals("sp3e3x", medicion.zonaId)
        assertEquals("agregado_1", medicion.agregadoId)
        assertEquals(62.5, medicion.nivelRuido, 0.0)
        assertEquals(1718000000000L, medicion.fechaHora)
    }

    @Test
    fun zonaPorDefectoTieneValoresEsperados() {
        val zona = Zona()

        assertEquals("", zona.geohashReducido)
        assertTrue(zona.ultimaActualizacion > 0L)
    }

    @Test
    fun zonaConValoresMantieneLaInformacion() {
        val zona = Zona(
            geohashReducido = "sp3e3x",
            ultimaActualizacion = 1718000000000L
        )

        assertEquals("sp3e3x", zona.geohashReducido)
        assertEquals(1718000000000L, zona.ultimaActualizacion)
    }

    @Test
    fun agregadoZonaPorDefectoTieneValoresEsperados() {
        val agregado = AgregadoZona()

        assertEquals("", agregado.agregadoId)
        assertEquals("", agregado.zonaId)
        assertEquals("", agregado.periodo)
        assertEquals(0L, agregado.fechaInicio)
        assertEquals(0L, agregado.fechaFin)
        assertEquals(0.0, agregado.nivelRuidoPromedio, 0.0)
        assertEquals(0, agregado.numeroMuestras)
    }

    @Test
    fun agregadoZonaConValoresMantieneLaInformacion() {
        val agregado = AgregadoZona(
            agregadoId = "agregado_1",
            zonaId = "sp3e3x",
            periodo = "15_minutos",
            fechaInicio = 1718000000000L,
            fechaFin = 1718000900000L,
            nivelRuidoPromedio = 58.3,
            numeroMuestras = 4
        )

        assertEquals("agregado_1", agregado.agregadoId)
        assertEquals("sp3e3x", agregado.zonaId)
        assertEquals("15_minutos", agregado.periodo)
        assertEquals(1718000000000L, agregado.fechaInicio)
        assertEquals(1718000900000L, agregado.fechaFin)
        assertEquals(58.3, agregado.nivelRuidoPromedio, 0.0)
        assertEquals(4, agregado.numeroMuestras)
    }
}