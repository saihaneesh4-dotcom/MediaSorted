package com.example.mediasorted

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile

object StorageExplorer {

    private const val TAG = "StorageExplorer"

    private const val PREFS_NAME = "MediaSortedPrefs"
    private const val ROOT_URI_KEY = "ROOT_URI"

    // Retrieve a requested folder using the previously saved URI
    fun getFolder(
        context: Context,
        relativePath: String
    ): DocumentFile? {

        try {

            // Step 1: Retrieve the saved URI
            val preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

            val savedUri = preferences.getString(
                ROOT_URI_KEY,
                null
            )

            if (savedUri == null) {
                Log.e(TAG, "SAVED ROOT URI NOT FOUND")
                return null
            }

            // Step 2: Convert the saved String into Uri
            val treeUri = Uri.parse(savedUri)

            // Step 3: Obtain the root DocumentFile
            val rootFolder = DocumentFile.fromTreeUri(
                context,
                treeUri
            ) ?: run {
                Log.e(TAG, "ROOT DOCUMENTFILE IS NULL")
                return null
            }

            var currentFolder: DocumentFile = rootFolder

            // Step 4: Navigate through the requested directory
            val folders = relativePath
                .split("/")
                .filter { it.isNotBlank() }

            for (folderName in folders) {

                val nextFolder = currentFolder.findFile(folderName)

                if (nextFolder == null || !nextFolder.isDirectory) {
                    Log.e(TAG, "FOLDER NOT FOUND: $folderName")
                    return null
                }

                currentFolder = nextFolder
            }

            Log.d(TAG, "FOLDER FOUND: $relativePath")

            return currentFolder

        } catch (e: SecurityException) {

            Log.e(TAG, "STORAGE PERMISSION ERROR", e)
            return null

        } catch (e: Exception) {

            Log.e(TAG, "FAILED TO ACCESS FOLDER", e)
            return null
        }
    }

    // Existing debugging functionality
    fun explore(root: DocumentFile?) {

        if (root == null) {
            Log.e("HANEESH_TEST", "ROOT IS NULL")
            return
        }

        Log.e("HANEESH_TEST", "========== ANDROID/MEDIA ==========")

        root.listFiles().forEach { appFolder ->

            Log.e(
                "HANEESH_TEST",
                "APP -> ${appFolder.name}"
            )

            if (appFolder.name == "com.whatsapp") {

                Log.e(
                    "HANEESH_TEST",
                    "FOUND WHATSAPP!"
                )

                exploreFolder(appFolder, 1)
            }
        }
    }

    private fun exploreFolder(
        folder: DocumentFile,
        level: Int
    ) {

        val indent = "    ".repeat(level)

        folder.listFiles().forEach { child ->

            Log.e(
                "HANEESH_TEST",
                indent + child.name
            )

            if (child.isDirectory) {
                exploreFolder(child, level + 1)
            }
        }
    }
}