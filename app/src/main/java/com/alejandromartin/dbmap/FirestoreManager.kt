package com.alejandromartin.dbmap

import com.alejandromartin.dbmap.model.Medicion
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun guardarMedicion(
        zonaId: String,
        nivelRuido: Double,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val docRef = db.collection("mediciones").document()

        val medicion = Medicion(
            medicionId = docRef.id,
            zonaId = zonaId,
            agregadoId = null,
            nivelRuido = nivelRuido,
            fechaHora = System.currentTimeMillis()
        )

        docRef.set(medicion)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}