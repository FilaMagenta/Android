package com.arnyminerz.filamagenta.firebase

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationManagerCompat
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

const val ALERTS_NOTIFICATION_CHANNEL = "alerts"

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Timber.i("Got new token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Got FCM notification. From: ${message.from}")

        val id = message.messageId?.hashCode() ?: (1..Int.MAX_VALUE).random()
        val notification = message.notification ?: return

        val builder = NotificationCompat.Builder(this, ALERTS_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.logo_magenta_mono)
            .setContentTitle(notification.title)
            .setContentText(notification.body?.substring(0, 30))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notification.body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else
                        0,
                )
            )
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PRIVATE)
        NotificationManagerCompat.from(this).notify(
            notification.tag,
            id,
            builder.build(),
        )
    }
}
