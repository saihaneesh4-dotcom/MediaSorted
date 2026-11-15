package com.example.mediasorted

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.MessageDigest

class NotificationService : NotificationListenerService() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var eventRepository: EventRepository

    override fun onCreate() {
        super.onCreate()

        val database = MediaDatabase.getDatabase(applicationContext)
        eventRepository = EventRepository(database.mediaEventDao())

        Log.e("HANEESH_TEST", "Notification Service Created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.e("HANEESH_TEST", "Notification Service Connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.packageName != "com.whatsapp") return

        // Ignore summary notifications.
        if (sbn.tag == null) {
            Log.e("HANEESH_TEST", "IGNORED SUMMARY NOTIFICATION")
            return
        }

        val extras = sbn.notification.extras

        val groupName =
            extras.getCharSequence(Notification.EXTRA_TITLE)
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "UNKNOWN"

        // Extract individual messages from MessagingStyle.
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)

        if (messages.isNullOrEmpty()) {
            Log.e("HANEESH_TEST", "NO INDIVIDUAL MESSAGES FOUND")
            return
        }

        // We only need the newest message, not the entire conversation history.
        val latestMessage = messages.lastOrNull() as? Bundle

        if (latestMessage == null) {
            Log.e("HANEESH_TEST", "LATEST MESSAGE COULD NOT BE READ")
            return
        }

        val messageText =
            latestMessage.getCharSequence("text")
                ?.toString()
                ?.trim()
                .orEmpty()

        val sender =
            latestMessage.getCharSequence("sender")
                ?.toString()
                ?.trim()
                .orEmpty()

        val messageTimestamp = latestMessage.getLong("time")

        if (messageText.isBlank() || messageTimestamp <= 0L) {
            Log.e("HANEESH_TEST", "INVALID MESSAGE DETAILS")
            return
        }

        Log.e("HANEESH_TEST", "========== LATEST WHATSAPP MESSAGE ==========")
        Log.e("HANEESH_TEST", "GROUP: $groupName")
        Log.e("HANEESH_TEST", "SENDER: $sender")
        Log.e("HANEESH_TEST", "MESSAGE: $messageText")
        Log.e("HANEESH_TEST", "MESSAGE TIME: $messageTimestamp")

        // Identify the media type.
        val mediaType = when {
            messageText.contains("photo", ignoreCase = true) -> "IMAGE"

            messageText.contains("video", ignoreCase = true) -> "VIDEO"

            messageText.contains(".pdf", ignoreCase = true) ||
                    messageText.contains(".doc", ignoreCase = true) ||
                    messageText.contains(".ppt", ignoreCase = true) ||
                    messageText.contains(".xls", ignoreCase = true) -> "DOCUMENT"

            messageText.contains("voice", ignoreCase = true) -> "VOICE_NOTE"

            else -> "UNKNOWN"
        }

        if (mediaType == "UNKNOWN") {
            Log.e("HANEESH_TEST", "IGNORED NON-MEDIA MESSAGE")
            return
        }

        // Build a stable identity for this message.
        val rawKey = listOf(
            sbn.packageName,
            groupName,
            sender,
            messageTimestamp.toString(),
            messageText
        ).joinToString("|")

        val eventKey = sha256(rawKey)

        val databaseEvent = MediaEventEntity(
            eventKey = eventKey,
            sourceApp = "WHATSAPP",
            groupName = groupName,
            mediaType = mediaType,
            notificationText = messageText,
            fileName = null,
            timestamp = messageTimestamp
        )

        serviceScope.launch {
            try {
                val insertedId = eventRepository.insertEvent(databaseEvent)

                if (insertedId == -1L) {
                    Log.e(
                        "HANEESH_TEST",
                        "DUPLICATE MESSAGE IGNORED -> $eventKey"
                    )
                } else {
                    Log.e(
                        "HANEESH_TEST",
                        "NEW EVENT SAVED -> ID=$insertedId GROUP=$groupName TYPE=$mediaType"
                    )
                }

            } catch (e: Exception) {
                Log.e("HANEESH_TEST", "ROOM SAVE FAILED", e)
            }
        }

        // Temporary compatibility with the existing FileObserver.
        val oldEvent = MediaEvent(
            sourceApp = SourceApp.WHATSAPP,
            groupName = groupName,
            mediaType = MediaType.valueOf(mediaType),
            timestamp = messageTimestamp,
            fileName = messageText
        )

        synchronized(MediaStore.pendingEvents) {
            val alreadyExists = MediaStore.pendingEvents.any {
                it.groupName == oldEvent.groupName &&
                        it.mediaType == oldEvent.mediaType &&
                        it.timestamp == oldEvent.timestamp &&
                        it.fileName == oldEvent.fileName
            }

            if (!alreadyExists) {
                MediaStore.pendingEvents.add(oldEvent)
            }
        }
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()

        Log.e("HANEESH_TEST", "Notification Service Destroyed")
    }
}