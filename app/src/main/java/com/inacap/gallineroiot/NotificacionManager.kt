package com.inacap.gallineroiot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore

class NotificacionManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val channelId = "alarma_gallinero"

    fun lanzarAlarma(faltantes: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarmas Gallinero", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("¡Alerta en el Gallinero!")
            .setContentText("Faltan $faltantes gallinas. Revisa el corral.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(1, builder.build())
        } catch (e: SecurityException) { }
    }

    fun registrarHistorial(codigoGallinero: String, faltantes: Int) {
        val registro = hashMapOf(
            "fecha" to System.currentTimeMillis(),
            "mensaje" to "Faltan $faltantes gallinas",
            "tipo" to "ALERTA"
        )
        db.collection("gallineros").document(codigoGallinero)
            .collection("historial").add(registro)
    }
}