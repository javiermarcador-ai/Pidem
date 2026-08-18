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
     * URI que Android propondrá inicialmente
     * al usuario cuando Pidem solicite
     * seleccionar la carpeta de almacenamiento.
     *
     * Actualmente:
     * Almacenamiento interno / Imágenes
     */
    fun getDefaultFolderUri(
        context: Context
    ): Uri {

        val path =
            context.getString(
                es.fotoindex.app.R.string.pidem_default_storage_path
            )

        return DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            path
        )
    }

    /**
     * Obtiene o crea la carpeta Pidem
     * dentro de la carpeta que el usuario
     * haya autorizado.
     */
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

        if (pidemFolder != null) {

            val noMediaFile =
                pidemFolder.findFile(".nomedia")

            if (noMediaFile == null) {

                pidemFolder.createFile(
                    "application/octet-stream",
                    ".nomedia"
                )
            }
        }

        return pidemFolder
    }

    /**
     * Crea una fotografía dentro de
     * la carpeta Pidem.
     */
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


    /**
     * Elimina físicamente una imagen de Pidem
     * a partir de la URI almacenada en Room.
     */
    fun deleteImage(
        context: Context,
        imagePath: String
    ): Boolean {

        return try {

            val uri = Uri.parse(imagePath)

            val document =
                DocumentFile.fromSingleUri(
                    context,
                    uri
                )

            document?.delete() == true

        } catch (_: Exception) {

            false
        }
    }

    /**
     * Elimina físicamente toda la carpeta Pidem.
     *
     * Esto elimina también:
     * - fotografías
     * - fotografías adjuntas
     * - .nomedia
     */
    fun deletePidemFolder(
        context: Context
    ): Boolean {

        return try {

            val storageUri = getStorageUri(context)
                    ?: return false

            val selectedFolder = DocumentFile.fromTreeUri(
                    context,
                    storageUri
                )
                    ?: return false

            val pidemFolder =
                selectedFolder.findFile("Pidem")

            /*
             * Si Pidem ya no existe,
             * consideramos que el objetivo
             * ya está cumplido.
             */
            if (pidemFolder == null) {  return true
            }
            pidemFolder.delete()

        } catch (_: Exception) {
            false
        }
    }

}