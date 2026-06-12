package com.alejandromartin.dbmap

import com.alejandromartin.dbmap.model.AgregadoZona
import com.alejandromartin.dbmap.model.Medicion
import com.alejandromartin.dbmap.model.Zona
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Gestiona la comunicación con Firebase Firestore.
 * Se encarga de guardar mediciones anónimas y consultar agregados de ruido por zona.
 * Trabaja con las colecciones "mediciones", "zonas" y "agregados_zona".
 */
class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Guarda una medición de ruido en Firestore de forma anónima.
     * Actualiza la zona correspondiente y recalcula el agregado del periodo de 15 minutos
     * usando una transacción atómica para garantizar la coherencia de los datos.
     *
     * @param zonaId Geohash reducido de la zona donde se realizó la medición.
     * @param nivelRuido Nivel de ruido estimado en decibelios.
     * @param onSuccess Callback ejecutado si el guardado finaliza correctamente.
     * @param onError Callback ejecutado si se produce un error, recibe la excepción.
     */
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

        // Referencias a los documentos afectados por la transacción
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

            // Recupera los valores actuales del agregado para recalcular el promedio
            val numeroMuestrasAnterior =
                agregadoSnapshot.getLong("numeroMuestras")?.toInt() ?: 0

            val promedioAnterior =
                agregadoSnapshot.getDouble("nivelRuidoPromedio") ?: 0.0

            val numeroMuestrasNuevo = numeroMuestrasAnterior + 1

            // Calcula el nuevo promedio incremental sin necesidad de cargar todas las mediciones
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

            // Escribe los tres documentos de forma atómica
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

    /**
     * Calcula el timestamp de inicio del periodo de 15 minutos al que pertenece una fechaHora.
     *
     * @param fechaHora Timestamp de la medición.
     * @return Timestamp de inicio del periodo correspondiente.
     */
    private fun obtenerInicioPeriodo(fechaHora: Long): Long {
        return (fechaHora / PERIODO_AGREGADO_MS) * PERIODO_AGREGADO_MS
    }

    /**
     * Genera el identificador único del agregado a partir de la zona y el inicio del periodo.
     *
     * @param zonaId Geohash reducido de la zona.
     * @param fechaInicio Timestamp de inicio del periodo.
     * @return Identificador en formato "zonaId_15min_fechaInicio".
     */
    private fun crearAgregadoId(zonaId: String, fechaInicio: Long): String {
        return "${zonaId}_15min_$fechaInicio"
    }

    /**
     * Consulta los últimos 100 agregados de ruido ordenados por fecha de inicio descendente.
     * Utilizado por HistoricoFragment y MapaFragment para mostrar los datos más recientes.
     *
     * @param onSuccess Callback con la lista de agregados obtenidos.
     * @param onError Callback ejecutado si se produce un error, recibe la excepción.
     */
    fun obtenerUltimosAgregados(
        onSuccess: (List<AgregadoZona>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("agregados_zona")
            .orderBy("fechaInicio", Query.Direction.DESCENDING)
            .limit(100)
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
        private const val PERIODO_AGREGADO_MS = 15 * 60 * 1000L  // Duración del periodo de agregación en milisegundos
    }
}