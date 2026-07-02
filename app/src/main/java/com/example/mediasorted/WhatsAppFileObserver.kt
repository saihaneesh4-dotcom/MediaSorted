package com.example.mediasorted

import android.os.Environment
import android.os.FileObserver
import android.util.Log
import java.io.File

class WhatsAppFileObserver : FileObserver(

    File(
        Environment.getExternalStorageDirectory(),
        "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"
    ).absolutePath,

    ALL_EVENTS
) {

    init {

        Log.e(
            "HANEESH_TEST",
            "IMAGE OBSERVER CREATED"
        )
    }

    override fun onEvent(event: Int, path: String?) {

        Log.e(
            "HANEESH_TEST",
            "---------------------------------"
        )

        Log.e(
            "HANEESH_TEST",
            "EVENT: $event"
        )

        Log.e(
            "HANEESH_TEST",
            "PATH: $path"
        )

        Log.e(
            "HANEESH_TEST",
            "---------------------------------"
        )
    }
}