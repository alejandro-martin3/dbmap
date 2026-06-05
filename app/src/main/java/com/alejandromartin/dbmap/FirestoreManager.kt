package com.alejandromartin.dbmap

import com.alejandromartin.dbmap.model.AgregadoZona
import com.alejandromartin.dbmap.model.Medicion
import com.alejandromartin.dbmap.model.Zona
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun guardarMedicion(
        zonaId: String,
        nivelRuido: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val fechaHora = System.currentTimeMillis()
        val fechaInicio = obtenerInicioPeriodo(fechaHora)
        val fechaFin = fechaInicio + PERIODO_AGREGADO_MS
        val agregadoId = crearAgregadoId(zonaId, fechaInicio)

        val medicionRef = db.collection("mediciones").document()
        val zonaRef = db.collection("zonas").document(zonaId)
        val agregadoRef = db.collection("agregados_zona").document(agregadoId)

        val zona = Zona(
            geohashReducido = zonaId,
            ultimaActualizacion = fechaHora
        )

        val medicion = Medicion(
            medicionId = medicionRef.id,
            zonaId = zonaId,
            agregadoId = agregadoId,
            nivelRuido = nivelRuido,
            fechaHora = fechaHora
        )

        db.runTransaction { transaction ->
            val agregadoSnapshot = transaction.get(agregadoRef)

            val numeroMuestrasAnterior =
                agregadoSnapshot.getLong("numeroMuestras")?.toInt() ?: 0

            val promedioAnterior =
                agregadoSnapshot.getDouble("nivelRuidoPromedio") ?: 0.0

            val numeroMuestrasNuevo = numeroMuestrasAnterior + 1

            val promedioNuevo =
                ((promedioAnterior * numeroMuestrasAnterior) + nivelRuido) / numeroMuestrasNuevo

            val agregadoZona = AgregadoZona(
                agregadoId = agregadoId,
                zonaId = zonaId,
                periodo = "15_minutos",
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                nivelRuidoPromedio = promedioNuevo,
                numeroMuestras = numeroMuestrasNuevo
            )

            transaction.set(zonaRef, zona)
            transaction.set(agregadoRef, agregadoZona)
            transaction.set(medicionRef, medicion)

            null
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onError(exception)
        }
    }

    private fun obtenerInicioPeriodo(fechaHora: Long): Long {
        return (fechaHora / PERIODO_AGREGADO_MS) * PERIODO_AGREGADO_MS
    }

    private fun crearAgregadoId(zonaId: String, fechaInicio: Long): String {
        return "${zonaId}_15min_$fechaInicio"
    }

    fun obtenerUltimosAgregados(
        onSuccess: (List<AgregadoZona>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("agregados_zona")
            .orderBy("fechaInicio", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val agregados = result.documents.mapNotNull { document ->
                    document.toObject(AgregadoZona::class.java)
                }
                onSuccess(agregados)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    companion object {
        private const val PERIODO_AGREGADO_MS = 15 * 60 * 1000L
    }
}