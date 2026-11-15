package com.example.mediasorted

data class MediaEvent(
    val sourceApp: SourceApp,
    val groupName: String,
    val mediaType: MediaType,
    val timestamp: Long,
    val fileName: String
)