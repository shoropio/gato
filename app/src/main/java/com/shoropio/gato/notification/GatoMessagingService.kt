package com.shoropio.gato.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GatoMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "GatoMessaging"
        var fcmToken: String? = null
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmToken = token
        Log.d(TAG, "New FCM token: $token")
        // Save token to Firestore for the current user
        val uid = com.shoropio.gato.data.FirebaseManager.getCurrentUid()
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .update("fcmToken", token)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving FCM token", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        val matchId = message.data["matchId"]
        val challengerName = message.data["challengerName"] ?: "Alguien"
        val title = message.notification?.title ?: "¡Nuevo desafío PvP!"
        val body = message.notification?.body ?: "$challengerName te ha retado a una partida"

        // Show notification
        NotificationHelper.createChannel(this)
        val intent = android.content.Intent(this, com.shoropio.gato.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "online_game/$matchId")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, matchId?.hashCode() ?: 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, "gato_challenges")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(android.app.NotificationManager::class.java)
        nm.notify(matchId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }
}
