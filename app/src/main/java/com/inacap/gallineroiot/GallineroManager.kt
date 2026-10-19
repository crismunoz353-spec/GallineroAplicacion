package com.inacap.gallineroiot

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class GallineroManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun crearGallinero(nombre: String, gallinasEsperadas: Int, onResult: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false, "Usuario no logueado")
        val codigo = "GALLINA-" + (1000..9999).random()

        val gallinero = hashMapOf(
            "nombre" to nombre,
            "codigo" to codigo,
            "propietarioId" to userId,
            "estadoPuerta" to "CERRADA",
            "gallinasEsperadas" to gallinasEsperadas,
            "gallinasActuales" to gallinasEsperadas
        )

        db.collection("gallineros").document(codigo).set(gallinero)
            .addOnSuccessListener { onResult(true, codigo) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun unirseAGallinero(codigo: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("gallineros").document(codigo).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(true, codigo)
                } else {
                    onResult(false, "El código de gallinero no existe")
                }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun escucharGallinero(codigo: String, onUpdate: (String, Int, Int) -> Unit): ListenerRegistration {
        return db.collection("gallineros").document(codigo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val estadoPuerta = snapshot.getString("estadoPuerta") ?: "CERRADA"
                    val gallinasEsperadas = snapshot.getLong("gallinasEsperadas")?.toInt() ?: 0
                    val gallinasActuales = snapshot.getLong("gallinasActuales")?.toInt() ?: 0
                    onUpdate(estadoPuerta, gallinasEsperadas, gallinasActuales)
                }
            }
    }

    fun cambiarEstadoPuerta(codigo: String, nuevoEstado: String) {
        db.collection("gallineros").document(codigo).update("estadoPuerta", nuevoEstado)
    }

    fun actualizarGallinas(codigo: String, cantidad: Int) {
        db.collection("gallineros").document(codigo).update("gallinasActuales", cantidad)
    }

    // NUEVA FUNCIÓN: Sumar gallina automáticamente
    fun sumarGallina(codigo: String, cantidadActual: Int) {
        db.collection("gallineros").document(codigo).update("gallinasActuales", cantidadActual + 1)
    }

    fun escucharHistorial(codigoGallinero: String, onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration {
        return db.collection("gallineros").document(codigoGallinero)
            .collection("historial")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val listaHistorial = snapshot.documents.map { it.data ?: emptyMap() }
                    onUpdate(listaHistorial)
                }
            }
    }
}