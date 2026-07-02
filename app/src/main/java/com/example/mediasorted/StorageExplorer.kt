package com.example.mediasorted

import android.util.Log
import androidx.documentfile.provider.DocumentFile

object StorageExplorer {

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