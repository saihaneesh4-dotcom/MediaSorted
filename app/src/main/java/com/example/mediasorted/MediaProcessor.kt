package com.example.mediasorted

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile

object MediaProcessor {

    private const val TAG = "MediaProcessor"

    fun processEvent(
        event: MediaEventEntity,
        context: Context
    ): Boolean {

        return try {

            // Step 1: Validate the event.
            val fileName = event.fileName
                ?: throw Exception("Event filename is missing")

            val sourceApp = SourceApp.valueOf(event.sourceApp)
            val mediaType = MediaType.valueOf(event.mediaType)

            // Step 2: Resolve the original folder.
            val folderPath = FolderResolver.getFolderPath(
                sourceApp,
                mediaType
            ) ?: throw Exception("Folder mapping not found")

            // Step 3: Access the original folder.
            val rootFolder = StorageExplorer.getFolder(
                context,
                folderPath
            ) ?: throw Exception("Unable to access folder: $folderPath")

            // Step 4: Locate the actual media file.
            val mediaFile = MediaMatcher.findFile(
                rootFolder,
                fileName
            ) ?: throw Exception("File not found: $fileName")

            // Step 5: Copy into the group's folder.
            val success = FileManager.copyDocument(
                context,
                mediaFile,
                event.groupName
            )

            if (success) {
                Log.e(TAG, "DOCUMENT PROCESSED SUCCESSFULLY")
                true
            } else {
                throw Exception("FileManager reported copy failure")
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "DOCUMENT PROCESSING FAILED -> ${e.message}",
                e
            )

            false
        }
    }
}