package com.example.mediasorted

data class MediaEvent(
    val groupName: String,
    val mediaType: String,
    val timestamp: Long,
    val fileName: String
)