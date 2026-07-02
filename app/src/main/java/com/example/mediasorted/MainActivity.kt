package com.example.mediasorted

import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile

class MainActivity : ComponentActivity() {

    private val folderPicker =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode != RESULT_OK) {
                Log.e("HANEESH_TEST", "USER CANCELLED")
                return@registerForActivityResult
            }

            val treeUri = result.data?.data

            if (treeUri == null) {
                Log.e("HANEESH_TEST", "URI IS NULL")
                return@registerForActivityResult
            }

            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            Log.e("HANEESH_TEST", "PERMISSION GRANTED")
            Log.e("HANEESH_TEST", "TREE URI = $treeUri")

            val root = DocumentFile.fromTreeUri(this, treeUri)

            if (root == null) {
                Log.e("HANEESH_TEST", "DOCUMENTFILE IS NULL")
                return@registerForActivityResult
            }

            StorageExplorer.explore(root)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("HANEESH_TEST", "APP STARTED")

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        val uri = DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/media"
        )

        intent.putExtra(
            DocumentsContract.EXTRA_INITIAL_URI,
            uri
        )

        folderPicker.launch(intent)
    }
}