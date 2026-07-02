package com.example.mediasorted

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.e("HANEESH_TEST", "Notification Service Connected")
    }

    // PASTE THE NEW FUNCTION HERE
    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val packageName = sbn.packageName

        Log.e("HANEESH_TEST", "Notification Received From: $packageName")

        if (packageName == "com.whatsapp") {

            val extras = sbn.notification.extras

            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text")

            Log.e("HANEESH_TEST", "Group: $title")
            Log.e("HANEESH_TEST", "Message: $text")

            val messageText = text?.toString() ?: ""

            val mediaType = when {

                messageText.contains("Photo", ignoreCase = true) ->
                    "PHOTO"

                messageText.contains("Video", ignoreCase = true) ->
                    "VIDEO"

                messageText.contains(".pdf", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".doc", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".docx", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".ppt", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".pptx", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".xls", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains(".xlsx", ignoreCase = true) ->
                    "DOCUMENT"

                messageText.contains("Voice", ignoreCase = true) ->
                    "VOICE"

                else ->
                    "UNKNOWN"
            }

            val event = MediaEvent(
                groupName = title ?: "UNKNOWN",
                mediaType = mediaType,
                timestamp = System.currentTimeMillis(),
                fileName = messageText
            )

            MediaStore.pendingEvents.add(event)

            Log.e(
                "HANEESH_TEST",
                "SAVED EVENT -> GROUP=${event.groupName} TYPE=${event.mediaType}"
            )
        }
    }
}