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
import android.os.Build

class NotificationService : NotificationListenerService() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var eventRepository: EventRepository
    private var documentObserver: WhatsAppDocumentObserver? = null

    private lateinit var chatRepository: ChatRepository

    private fun startDocumentObserver() {

        if (documentObserver != null) {
            Log.e("HANEESH_TEST", "DOCUMENT OBSERVER ALREADY RUNNING")
            return
        }

        val observer = WhatsAppDocumentObserver(
            context = applicationContext,
            eventRepository = eventRepository,
            observerScope = serviceScope
        )

        observer.startWatching()

        documentObserver = observer

        Log.e("HANEESH_TEST", "DOCUMENT OBSERVER STARTED")
    }

    override fun onCreate() {
        super.onCreate()

        val database = MediaDatabase.getDatabase(applicationContext)
        eventRepository = EventRepository(database.mediaEventDao())

        chatRepository = ChatRepository(database.chatDao())

        Log.e("HANEESH_TEST", "Notification Service Created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.e("HANEESH_TEST", "Notification Service Connected")

        startDocumentObserver()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        documentObserver?.stopWatching()
        documentObserver = null

        Log.e("HANEESH_TEST", "DOCUMENT OBSERVER STOPPED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.packageName != "com.whatsapp") return

        // Ignore summary notifications.
        if (sbn.tag == null) {
            Log.e("HANEESH_TEST", "IGNORED SUMMARY NOTIFICATION")
            return
        }

        val extras = sbn.notification.extras

// ==========================================
// WHATSAPP NOTIFICATION INSPECTOR
// ==========================================

        Log.e("WA_INSPECTOR", "========== NOTIFICATION DETAILS ==========")

        Log.e("WA_INSPECTOR", "PACKAGE: ${sbn.packageName}")
        Log.e("WA_INSPECTOR", "NOTIFICATION KEY: ${sbn.key}")
        Log.e("WA_INSPECTOR", "NOTIFICATION ID: ${sbn.id}")
        Log.e("WA_INSPECTOR", "NOTIFICATION TAG: ${sbn.tag}")
        Log.e("WA_INSPECTOR", "GROUP KEY: ${sbn.groupKey}")

        val shortcutId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sbn.notification.shortcutId
                ?.takeIf { it.isNotBlank() }
        } else {
            null
        }

        Log.e("WA_INSPECTOR", "SHORTCUT ID: $shortcutId")

// Notification extras
        Log.e("WA_INSPECTOR", "EXTRA KEYS: ${extras.keySet()}")

        Log.e(
            "WA_INSPECTOR",
            "EXTRA_TITLE: ${extras.getCharSequence(Notification.EXTRA_TITLE)}"
        )

        Log.e(
            "WA_INSPECTOR",
            "EXTRA_CONVERSATION_TITLE: ${
                extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            }"
        )

        Log.e(
            "WA_INSPECTOR",
            "HAS GROUP FLAG: ${
                extras.containsKey(Notification.EXTRA_IS_GROUP_CONVERSATION)
            }"
        )

        Log.e(
            "WA_INSPECTOR",
            "IS GROUP CONVERSATION: ${
                extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
            }"
        )

// Additional messaging information
        Log.e(
            "WA_INSPECTOR",
            "MESSAGING USER: ${extras.get(Notification.EXTRA_MESSAGING_PERSON)}"
        )

        Log.e(
            "WA_INSPECTOR",
            "PEOPLE LIST: ${extras.get(Notification.EXTRA_PEOPLE_LIST)}"
        )


        val groupName =
            extras.getCharSequence(Notification.EXTRA_TITLE)
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "UNKNOWN"

        // Extract individual messages from MessagingStyle.
        val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelableArray(
                Notification.EXTRA_MESSAGES,
                Bundle::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        }

// Inspect all messages supplied by WhatsApp
        Log.e(
            "WA_INSPECTOR",
            "MESSAGE COUNT: ${messages?.size ?: 0}"
        )

        messages?.forEachIndexed { index, parcelable ->

            val message = parcelable as? Bundle

            if (message != null) {

                Log.e("WA_INSPECTOR", "------ MESSAGE $index ------")

                Log.e(
                    "WA_INSPECTOR",
                    "MESSAGE KEYS: ${message.keySet()}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "TEXT: ${message.getCharSequence("text")}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "SENDER: ${message.get("sender")}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "TIMESTAMP: ${message.getLong("time")}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "SENDER PERSON: ${message.get("sender_person")}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "MIME TYPE: ${message.getString("type")}"
                )

                Log.e(
                    "WA_INSPECTOR",
                    "DATA URI: ${message.get("uri")}"
                )
            }
        }

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
        // Extract the actual document filename, if present.
        val documentFileName = extractDocumentFileName(messageText)

        Log.e(
            "HANEESH_TEST",
            "EXTRACTED DOCUMENT FILENAME: $documentFileName"
        )

// Identify the media type.
        val mediaType = when {

            messageText.contains("photo", ignoreCase = true) -> "IMAGE"

            messageText.contains("video", ignoreCase = true) -> "VIDEO"

            documentFileName != null -> "DOCUMENT"

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
            fileName = documentFileName,
            timestamp = messageTimestamp
        )

        serviceScope.launch {
            try {
                if (shortcutId != null && groupName != "UNKNOWN") {

                    val chatResult = chatRepository.registerOrUpdateChat(
                        chatId = shortcutId,
                        displayName = groupName
                    )

                    Log.e("CHAT_ID_TEST", "SHORTCUT ID: $shortcutId")
                    Log.e("CHAT_ID_TEST", "CURRENT NAME: ${chatResult.chat.displayName}")
                    Log.e("CHAT_ID_TEST", "IS NEW CHAT: ${chatResult.isNewChat}")
                    Log.e("CHAT_ID_TEST", "NAME CHANGED: ${chatResult.nameChanged}")
                    Log.e("CHAT_ID_TEST", "PREVIOUS NAME: ${chatResult.previousName}")
                }
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
            fileName = documentFileName ?: messageText
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

    private fun extractDocumentFileName(messageText: String): String? {

        // Remove WhatsApp's page count, such as "(32 pages)".
        val withoutPageCount = messageText.replace(
            Regex("""\s+\(\d+\s+pages?\)\s*$""", RegexOption.IGNORE_CASE),
            ""
        ).trim()

        // Remove the document icon shown in WhatsApp notifications.
        val cleanedText = withoutPageCount
            .removePrefix("📄")
            .trim()

        // Supported document extensions.
        val fileNameRegex = Regex(
            """(.+\.(?:pdf|docx?|pptx?|xlsx?|txt|rtf|odt|ods|odp|csv|zip))$""",
            RegexOption.IGNORE_CASE
        )

        return fileNameRegex.find(cleanedText)
            ?.groupValues
            ?.get(1)
            ?.trim()
    }
    private fun sha256(value: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroy() {
        documentObserver?.stopWatching()
        documentObserver = null

        serviceScope.cancel()

        super.onDestroy()

        Log.e("HANEESH_TEST", "Notification Service Destroyed")
    }
}