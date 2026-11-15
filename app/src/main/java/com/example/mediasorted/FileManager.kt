package com.example.mediasorted


import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.documentfile.provider.DocumentFile

object FileManager {


    fun copyDocument(
        context: Context,
        sourceFile: DocumentFile,
        groupName: String
    ): Boolean {

        return try {

            // Retrieve the SAF root URI
            val preferences = context.getSharedPreferences(
                "MediaSortedPrefs",
                Context.MODE_PRIVATE
            )

            val rootUriString = preferences.getString("ROOT_URI", null)
                ?: throw Exception("SAF root URI not found")

            val rootUri = Uri.parse(rootUriString)

            val rootFolder = DocumentFile.fromTreeUri(context, rootUri)
                ?: throw Exception("Unable to access SAF root folder")

            // Create or access MediaSorted/Documents
            val mediaSortedFolder = getOrCreateDirectory(
                rootFolder,
                "MediaSorted"
            )

            val documentsFolder = getOrCreateDirectory(
                mediaSortedFolder,
                "Documents"
            )

            // Sanitize group name
            val safeGroupName = groupName
                .trim()
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .ifBlank { "Unknown" }

            val groupFolder = getOrCreateDirectory(
                documentsFolder,
                safeGroupName
            )

            // Generate a unique filename if one already exists
            val originalName = sourceFile.name
                ?: throw Exception("Source filename is missing")

            val destinationName = getUniqueFileName(
                groupFolder,
                originalName
            )

            // Create destination file
            val destinationFile = groupFolder.createFile(
                sourceFile.type ?: "application/octet-stream",
                destinationName
            ) ?: throw Exception("Unable to create destination file")

            // Copy file using ContentResolver
            val inputStream = context.contentResolver
                .openInputStream(sourceFile.uri)
                ?: throw Exception("Unable to open source file")

            val outputStream = context.contentResolver
                .openOutputStream(destinationFile.uri, "w")
                ?: throw Exception("Unable to open destination file")

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Log.e(
                "HANEESH_TEST",
                "SAF COPY SUCCESS -> ${destinationFile.uri}"
            )

            true

        } catch (e: Exception) {

            Log.e(
                "HANEESH_TEST",
                "SAF COPY FAILED -> ${e.message}",
                e
            )

            false
        }
    }


    private fun getOrCreateDirectory(
        parent: DocumentFile,
        folderName: String
    ): DocumentFile {

        val existingFolder = parent.findFile(folderName)

        if (existingFolder != null && existingFolder.isDirectory) {
            return existingFolder
        }

        return parent.createDirectory(folderName)
            ?: throw Exception("Unable to create folder: $folderName")
    }


    private fun getUniqueFileName(
        folder: DocumentFile,
        originalName: String
    ): String {

        if (folder.findFile(originalName) == null) {
            return originalName
        }

        val dotIndex = originalName.lastIndexOf('.')

        val name = if (dotIndex > 0) {
            originalName.substring(0, dotIndex)
        } else {
            originalName
        }

        val extension = if (dotIndex > 0) {
            originalName.substring(dotIndex)
        } else {
            ""
        }

        var counter = 1
        var newName: String

        do {
            newName = "${name}_$counter$extension"
            counter++
        } while (folder.findFile(newName) != null)

        return newName
    }

    fun copyDocument(
        sourceFile: File,
        groupName: String
    ): Boolean {

        return try {

            val mediaSortedFolder = File(
                Environment.getExternalStorageDirectory(),
                "MediaSorted/Documents/$groupName"
            )

            if (!mediaSortedFolder.exists()) {
                mediaSortedFolder.mkdirs()
            }

            val destinationFile = File(
                mediaSortedFolder,
                sourceFile.name
            )

            FileInputStream(sourceFile).use { input ->

                FileOutputStream(destinationFile).use { output ->

                    input.copyTo(output)
                }
            }

            Log.e(
                "HANEESH_TEST",
                "COPY SUCCESS -> ${destinationFile.absolutePath}"
            )

            true

        } catch (e: Exception) {

            Log.e(
                "HANEESH_TEST",
                "COPY FAILED -> ${e.message}"
            )

            false
        }
    }


    fun testMediaStorePdf(
        context: Context
    ) {

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.SIZE
        )

        val collection =
            MediaStore.Files.getContentUri("external")

        val cursor = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )

        if (cursor == null) {

            Log.e(
                "HANEESH_TEST",
                "MEDIASTORE QUERY FAILED"
            )

            return
        }

        if (!cursor.moveToFirst()) {

            Log.e(
                "HANEESH_TEST",
                "MEDIASTORE IS EMPTY"
            )

            cursor.close()
            return
        }

        Log.e(
            "HANEESH_TEST",
            "========== ALL FILES =========="
        )

        do {

            val id = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    MediaStore.Files.FileColumns._ID
                )
            )

            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    MediaStore.Files.FileColumns.DISPLAY_NAME
                )
            )

            val mime = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
            )

            val relativePath = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    MediaStore.Files.FileColumns.RELATIVE_PATH
                )
            )

            val size = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    MediaStore.Files.FileColumns.SIZE
                )
            )

            val uri = Uri.withAppendedPath(
                collection,
                id.toString()
            )

            Log.e(
                "HANEESH_TEST",
                "--------------------------------"
            )

            Log.e(
                "HANEESH_TEST",
                "NAME : $name"
            )

            Log.e(
                "HANEESH_TEST",
                "MIME : $mime"
            )

            Log.e(
                "HANEESH_TEST",
                "PATH : $relativePath"
            )

            Log.e(
                "HANEESH_TEST",
                "SIZE : $size"
            )

            Log.e(
                "HANEESH_TEST",
                "URI  : $uri"
            )

        } while (cursor.moveToNext())

        cursor.close()
    }
}