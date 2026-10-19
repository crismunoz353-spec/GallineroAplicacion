package com.inacap.gallineroiot

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registrarUsuario(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (password.length < 8) {
            onResult(false, "La contraseña debe tener al menos 8 caracteres")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Error al registrar")
                }
            }
    }

    fun iniciarSesion(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Error al iniciar sesión")
                }
            }
    }

    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser

    fun cerrarSesion() {
        auth.signOut()
    }
}