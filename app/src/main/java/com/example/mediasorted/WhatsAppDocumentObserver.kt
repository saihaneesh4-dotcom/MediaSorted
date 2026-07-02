package com.example.mediasorted

import android.os.Environment
import android.os.FileObserver
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class WhatsAppDocumentObserver : FileObserver(

    File(
        Environment.getExternalStorageDirectory(),
        "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"
    ).absolutePath,

    ALL_EVENTS
) {

    override fun onEvent(event: Int, path: String?) {

        if (event != CREATE) return

        Log.e(
            "HANEESH_TEST",
            "DOC EVENT -> EVENT=$event PATH=$path"
        )

        if (path == null) return

        val matchingEvent = MediaStore.pendingEvents.lastOrNull {

            it.mediaType == "DOCUMENT"
        }

        if (matchingEvent != null) {

            val cleanGroupName = matchingEvent.groupName
                .replace("WhatsApp: ", "")
                .trim()

            Log.e(
                "HANEESH_TEST",
                "MATCH FOUND -> GROUP=$cleanGroupName"
            )

            Log.e(
                "HANEESH_TEST",
                "NOTIFICATION FILE -> ${matchingEvent.fileName}"
            )

            Log.e(
                "HANEESH_TEST",
                "DOWNLOADED FILE -> $path"
            )

            val sourceFile = File(
                Environment.getExternalStorageDirectory(),
                "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/$path"
            )

            Log.e(
                "HANEESH_TEST",
                "SOURCE FILE EXISTS -> ${sourceFile.exists()}"
            )

            Log.e(
                "HANEESH_TEST",
                "SOURCE PATH -> ${sourceFile.absolutePath}"
            )
            if (sourceFile.exists()) {

                val copied = FileManager.copyDocument(
                    sourceFile,
                    cleanGroupName
                )

                if (copied) {

                    Log.e(
                        "HANEESH_TEST",
                        "DOCUMENT COPIED SUCCESSFULLY"
                    )

                } else {

                    Log.e(
                        "HANEESH_TEST",
                        "DOCUMENT COPY FAILED"
                    )
                }
            }
        }
    }
}