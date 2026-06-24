package com.shoropio.gato.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shoropio.gato.MainActivity
import com.shoropio.gato.R

object NotificationHelper {
    private const val CHANNEL_ID = "gato_challenges"
    private const val CHANNEL_NAME = "Desafíos PvP"

    fun createChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones cuando un amigo te reta a una partida"
        }
        nm.createNotificationChannel(channel)
    }

    fun showChallengeNotification(
        context: Context,
        matchId: String,
        challengerName: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "online_game/$matchId")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, matchId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Nuevo desafío PvP!")
            .setContentText("$challengerName te ha retado a una partida")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(matchId.hashCode(), notification)
    }
}
