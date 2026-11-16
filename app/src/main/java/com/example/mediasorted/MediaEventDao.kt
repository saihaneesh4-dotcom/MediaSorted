package com.example.mediasorted

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaEventDao {

    // Insert a new event.
    // Returns -1 if an event with the same eventKey already exists.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: MediaEventEntity): Long

    // Retrieve all media events
    @Query("SELECT * FROM media_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<MediaEventEntity>>

    // Retrieve events waiting to be processed
    @Query(
        "SELECT * FROM media_events " +
                "WHERE status = 'PENDING' ORDER BY timestamp ASC"
    )
    suspend fun getPendingEvents(): List<MediaEventEntity>

    // Find pending document events using the exact filename.
    @Query(
        """
    SELECT * FROM media_events
    WHERE fileName = :fileName
    AND mediaType = 'DOCUMENT'
    AND sourceApp = 'WHATSAPP'
    AND status = 'PENDING'
    ORDER BY timestamp ASC
    """
    )
    suspend fun getPendingDocumentEventsByFileName(
        fileName: String
    ): List<MediaEventEntity>

    // Claim an event so that only one callback can process it.
    @Query(
        """
    UPDATE media_events
    SET status = 'PROCESSING'
    WHERE id = :eventId AND status = 'PENDING'
    """
    )
    suspend fun claimEvent(eventId: Int): Int


    // Mark an event as completed after successful copying.
    @Query(
        """
    UPDATE media_events
    SET status = 'COMPLETED',
        lastError = NULL
    WHERE id = :eventId AND status = 'PROCESSING'
    """
    )
    suspend fun completeEvent(eventId: Int): Int


    // Return a failed event to PENDING for a future retry.
    @Query(
        """
    UPDATE media_events
    SET status = 'PENDING',
        retryCount = retryCount + 1,
        lastError = :error
    WHERE id = :eventId AND status = 'PROCESSING'
    """
    )
    suspend fun failEvent(eventId: Int, error: String): Int

    // Retrieve an event using its ID
    @Query("SELECT * FROM media_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): MediaEventEntity?

    // Check whether an event already exists
    @Query("SELECT EXISTS(SELECT 1 FROM media_events WHERE eventKey = :eventKey)")
    suspend fun eventExists(eventKey: String): Boolean

    // Update an existing event
    @Update
    suspend fun updateEvent(event: MediaEventEntity)

    // Delete an event
    @Delete
    suspend fun deleteEvent(event: MediaEventEntity)

    // Delete all completed events
    @Query("DELETE FROM media_events WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedEvents()
}