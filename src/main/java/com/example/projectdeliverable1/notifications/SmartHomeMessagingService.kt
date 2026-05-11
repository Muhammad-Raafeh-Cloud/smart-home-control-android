package com.example.projectdeliverable1.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SmartHomeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token can be saved to Firestore if targeted push notifications are needed.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Smart Home Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "New cloud message received."
        NotificationHelper.showNotification(applicationContext, title, body, 5001)
    }
}
