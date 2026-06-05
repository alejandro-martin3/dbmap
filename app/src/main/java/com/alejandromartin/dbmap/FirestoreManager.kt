package com.alejandromartin.dbmap

import com.alejandromartin.dbmap.model.Medicion
import com.alejandromartin.dbmap.model.Zona
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun guardarMedicion(
        zonaId: String,
        nivelRuido: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val medicionRef = db.collection("mediciones").document()
        val zonaRef = db.collection("zonas").document(zonaId)

        val centroZona = ZoneManager.getApproximateCenter(zonaId)

        val zona = Zona(
            zonaId = zonaId,
            geohashReducido = zonaId,
            nombreZona = null,
            centroLatitud = centroZona.first,
            centroLongitud = centroZona.second
        )

        val medicion = Medicion(
            medicionId = medicionRef.id,
            zonaId = zonaId,
            agregadoId = null,
            nivelRuido = nivelRuido,
            fechaHora = System.currentTimeMillis()
        )

        val batch = db.batch()

        batch.set(zonaRef, zona, SetOptions.merge())
        batch.set(medicionRef, medicion)

        batch.commit()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}