package com.example.mediasorted

import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class WhatsAppDocumentObserver(
    private val context: Context,
    private val eventRepository: EventRepository,
    private val observerScope: CoroutineScope
) : FileObserver(
    File(
        Environment.getExternalStorageDirectory(),
        "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"
    ).absolutePath,
    CLOSE_WRITE or CLOSE_NOWRITE
) {

    companion object {
        private const val TAG = "HANEESH_TEST"
    }

    // Prevent simultaneous processing of the same filename.
    private val processingPaths = mutableSetOf<String>()
    private val processingLock = Any()

    init {
        Log.e(TAG, "DOCUMENT OBSERVER CREATED")
    }

    override fun onEvent(event: Int, path: String?) {

        if (path == null) return

        val isCloseWrite = event and CLOSE_WRITE != 0
        val isCloseNoWrite = event and CLOSE_NOWRITE != 0

        if (!isCloseWrite && !isCloseNoWrite) return

        // Ignore duplicate callbacks while this file is being processed.
        val added = synchronized(processingLock) {
            processingPaths.add(path)
        }

        if (!added) {
            Log.d(TAG, "DUPLICATE EVENT IGNORED -> $path")
            return
        }

        Log.e(TAG, "DOCUMENT CLOSE EVENT -> $path")

        val sourceFile = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/$path"
        )

        observerScope.launch {

            var claimedEventId: Int? = null

            try {

                delay(500)

                if (!sourceFile.exists() || !sourceFile.isFile) {
                    Log.e(TAG, "FILE NOT FOUND -> $path")
                    return@launch
                }

                // Check whether the file size is stable.
                val firstSize = sourceFile.length()

                delay(500)

                val secondSize = sourceFile.length()

                if (firstSize == 0L || firstSize != secondSize) {
                    Log.e(TAG, "FILE NOT READY -> $path")
                    return@launch
                }

                Log.e(TAG, "FILE READY -> $path")
                Log.e(TAG, "FILE SIZE -> $secondSize")

                // Find the corresponding pending Room event.
                val matchingEvents =
                    eventRepository.getPendingDocumentEventsByFileName(path)

                if (matchingEvents.isEmpty()) {
                    Log.e(
                        TAG,
                        "NO EXACT ROOM MATCH FOR FILE -> $path"
                    )
                    return@launch
                }

                if (matchingEvents.size > 1) {
                    Log.e(
                        TAG,
                        "AMBIGUOUS MATCH -> ${matchingEvents.size} EVENTS"
                    )
                    return@launch
                }

                val matchedEvent = matchingEvents.first()

                Log.e(TAG, "MATCH FOUND -> EVENT ${matchedEvent.id}")
                Log.e(TAG, "GROUP -> ${matchedEvent.groupName}")
                Log.e(TAG, "FILENAME -> ${matchedEvent.fileName}")

                // Atomically claim the event.
                val claimed = eventRepository.claimEvent(matchedEvent.id)

                if (claimed != 1) {
                    Log.e(
                        TAG,
                        "EVENT ALREADY CLAIMED OR PROCESSED -> ${matchedEvent.id}"
                    )
                    return@launch
                }

                claimedEventId = matchedEvent.id

                Log.e(TAG, "EVENT CLAIMED -> ${matchedEvent.id}")

                // Perform the actual SAF copy.
                val success = MediaProcessor.processEvent(
                    matchedEvent,
                    context
                )

                if (success) {

                    val completed =
                        eventRepository.completeEvent(matchedEvent.id)

                    if (completed == 1) {
                        Log.e(
                            TAG,
                            "EVENT COMPLETED SUCCESSFULLY -> ${matchedEvent.id}"
                        )
                    } else {
                        Log.e(
                            TAG,
                            "COPY SUCCEEDED BUT STATUS UPDATE FAILED -> ${matchedEvent.id}"
                        )
                    }

                    claimedEventId = null

                } else {

                    eventRepository.failEvent(
                        matchedEvent.id,
                        "MediaProcessor failed to copy document"
                    )

                    Log.e(
                        TAG,
                        "EVENT RETURNED TO PENDING -> ${matchedEvent.id}"
                    )

                    claimedEventId = null
                }

            } catch (e: Exception) {

                Log.e(TAG, "DOCUMENT PROCESSING ERROR", e)

                // If an exception occurred after claiming the event,
                // return it to PENDING so it can be retried.
                claimedEventId?.let { eventId ->
                    try {
                        eventRepository.failEvent(
                            eventId,
                            e.message ?: "Unknown processing error"
                        )
                    } catch (updateError: Exception) {
                        Log.e(TAG, "FAILED TO RESET EVENT STATUS", updateError)
                    }
                }

            } finally {

                synchronized(processingLock) {
                    processingPaths.remove(path)
                }
            }
        }
    }
}