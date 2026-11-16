package com.example.mediasorted

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val mediaEventDao: MediaEventDao
) {

    // Insert an event. Returns -1 if it is a duplicate.
    suspend fun insertEvent(event: MediaEventEntity): Long {
        return mediaEventDao.insertEvent(event)
    }

    // Observe all events
    fun getAllEvents(): Flow<List<MediaEventEntity>> {
        return mediaEventDao.getAllEvents()
    }

    // Get events waiting to be processed
    suspend fun getPendingEvents(): List<MediaEventEntity> {
        return mediaEventDao.getPendingEvents()
    }

    // Find pending WhatsApp documents by filename.
    suspend fun getPendingDocumentEventsByFileName(
        fileName: String
    ): List<MediaEventEntity> {
        return mediaEventDao.getPendingDocumentEventsByFileName(fileName)
    }

    // Get an event by its database ID
    suspend fun getEventById(eventId: Int): MediaEventEntity? {
        return mediaEventDao.getEventById(eventId)
    }

    // Check whether an event already exists
    suspend fun eventExists(eventKey: String): Boolean {
        return mediaEventDao.eventExists(eventKey)
    }

    // Update an event
    suspend fun updateEvent(event: MediaEventEntity) {
        mediaEventDao.updateEvent(event)
    }

    // Delete an event
    suspend fun deleteEvent(event: MediaEventEntity) {
        mediaEventDao.deleteEvent(event)
    }

    // Delete completed events
    suspend fun deleteCompletedEvents() {
        mediaEventDao.deleteCompletedEvents()
    }

    suspend fun claimEvent(eventId: Int): Int {
        return mediaEventDao.claimEvent(eventId)
    }

    suspend fun completeEvent(eventId: Int): Int {
        return mediaEventDao.completeEvent(eventId)
    }

    suspend fun failEvent(eventId: Int, error: String): Int {
        return mediaEventDao.failEvent(eventId, error)
    }
}