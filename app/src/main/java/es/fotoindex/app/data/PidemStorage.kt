package es.fotoindex.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

object PidemStorage {

    private const val PREFS_NAME = "pidem_storage"
    private const val STORAGE_URI = "storage_uri"

    fun getStorageUri(context: Context): Uri? {

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val uriString = prefs.getString(
            STORAGE_URI,
            null
        )

        return uriString?.let {
            Uri.parse(it)
        }
    }

    fun saveStorageUri(
        context: Context,
        uri: Uri
    ) {

        val flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        context.contentResolver.takePersistableUriPermission(
            uri,
            flags
        )

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                STORAGE_URI,
                uri.toString()
            )
            .apply()
    }

    /**
     * Carpeta que Android propondrá inicialmente
     * cuando Pidem solicite al usuario elegir
     * dónde almacenar sus fotografías.
     *
     * Por defecto: Almacenamiento interno / Imágenes
     * (Pictures en Android).
     */
    fun getDefaultFolderUri(): Uri {

        return DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Pictures"
        )
    }

    fun getPidemFolder(
        context: Context
    ): DocumentFile? {

        val storageUri =
            getStorageUri(context)
                ?: return null

        val selectedFolder =
            DocumentFile.fromTreeUri(
                context,
                storageUri
            )
                ?: return null

        var pidemFolder =
            selectedFolder.findFile("Pidem")

        if (pidemFolder == null) {

            pidemFolder =
                selectedFolder.createDirectory(
                    "Pidem"
                )
        }

        return pidemFolder
    }

    fun createImageFile(
        context: Context,
        fileName: String
    ): DocumentFile? {

        val pidemFolder =
            getPidemFolder(context)
                ?: return null

        return pidemFolder.createFile(
            "image/jpeg",
            fileName
        )
    }
}