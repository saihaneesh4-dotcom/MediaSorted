package com.example.mediasorted

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_events",
    indices = [
        Index(value = ["eventKey"], unique = true)
    ]
)
data class MediaEventEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Unique identity of the actual WhatsApp message
    val eventKey: String,

    val sourceApp: String,

    val groupName: String,

    val mediaType: String,

    val notificationText: String,

    val fileName: String? = null,

    val timestamp: Long,

    val status: String = "PENDING",

    val retryCount: Int = 0,

    val lastError: String? = null
)