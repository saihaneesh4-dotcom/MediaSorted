package com.example.mediasorted

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile

object MediaProcessor {

    private const val TAG = "MediaProcessor"

    fun processNotification(
        event: MediaEvent,
        context: Context
    ) {

        // Step 1: Resolve the original folder
        val folderPath = resolveFolder(
            event.sourceApp,
            event.mediaType
        ) ?: run {
            Log.e(TAG, "Folder mapping not found")
            return
        }

        // Step 2: Access the original folder
        val rootFolder = accessFolder(
            context,
            folderPath
        ) ?: run {
            Log.e(TAG, "Unable to access folder: $folderPath")
            return
        }

        // Step 3: Locate the actual media file
        val mediaFile = locateFile(
            rootFolder,
            event.fileName
        ) ?: run {
            Log.e(TAG, "File not found: ${event.fileName}")
            return
        }

        // Step 4: Organize the media file
        val success = organizeFile(
            context = context,
            mediaFile = mediaFile,
            groupName = event.groupName
        )

        if (success) {
            Log.d(TAG, "File organized successfully")
        } else {
            Log.e(TAG, "Failed to organize file")
        }
    }

    private fun resolveFolder(
        sourceApp: SourceApp,
        mediaType: MediaType
    ): String? {

        return FolderResolver.getFolderPath(
            sourceApp,
            mediaType
        )
    }

    private fun accessFolder(
        context: Context,
        folderPath: String
    ): DocumentFile? {

        return StorageExplorer.getFolder(
            context,
            folderPath
        )
    }

    private fun locateFile(
        rootFolder: DocumentFile,
        fileName: String
    ): DocumentFile? {

        return MediaMatcher.findFile(
            rootFolder,
            fileName
        )
    }

    private fun organizeFile(
        context: Context,
        mediaFile: DocumentFile,
        groupName: String
    ): Boolean {

        return FileManager.copyDocument(
            context,
            mediaFile,
            groupName
        )
    }
}