package com.example.mediasorted

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileManager {

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