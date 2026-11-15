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