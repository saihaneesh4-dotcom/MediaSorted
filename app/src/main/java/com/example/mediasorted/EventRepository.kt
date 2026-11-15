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
}