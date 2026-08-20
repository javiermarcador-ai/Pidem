package es.fotoindex.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object PidemImporter {

    private const val FIELD_SEPARATOR =
        "/$3p4r4t*rYúnke"

    private const val EXPORT_VERSION =
        "PIDEM_EXPORT_VERSION=1"


    data class ImportedData(
        val categories: List<ImportedCategory>,
        val photos: List<ImportedPhoto>,
        val attachments: List<ImportedAttachment>
    )


    data class ImportedCategory(
        val oldId: Long,
        val name: String
    )


    data class ImportedPhoto(
        val oldId: Long,
        val firstPhotoEntry: String,
        val secondPhotoEntry: String?,
        val ocrText: String,
        val additionalText: String,
        val category: String,
        val createdAt: Long
    )


    data class ImportedAttachment(
        val oldId: Long,
        val oldPhotoId: Long,
        val imageEntry: String,
        val createdAt: Long
    )


    /*
     * =============================================================
     * LEER ARCHIVO DE IMPORTACIÓN
     * =============================================================
     */

    suspend fun import(
        context: Context,
        fileUri: Uri,
        onProgress: (String) -> Unit = {}
    ): Result<ImportedData> {

        return try {

            onProgress(
                "Leyendo archivo de importación"
            )

            val data =
                readData(
                    context = context,
                    fileUri = fileUri,
                    onProgress = onProgress
                )

            Result.success(
                data
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    /*
     * =============================================================
     * LEER DATOS
     * =============================================================
     */

    private fun readData(
        context: Context,
        fileUri: Uri,
        onProgress: (String) -> Unit
    ): ImportedData {

        val categories =
            mutableListOf<ImportedCategory>()

        val photos =
            mutableListOf<ImportedPhoto>()

        val attachments =
            mutableListOf<ImportedAttachment>()

        var currentSection = ""

        val inputStream =
            context.contentResolver.openInputStream(
                fileUri
            )
                ?: throw Exception(
                    "No se pudo abrir el archivo de importación"
                )


        ZipInputStream(
            BufferedInputStream(
                inputStream
            )
        ).use { zip ->

            var entry =
                zip.nextEntry

            var versionFound =
                false

            while (entry != null) {

                if (entry.isDirectory) {

                    zip.closeEntry()
                    entry = zip.nextEntry
                    continue
                }

                if (
                    entry.name == "pidem_data.txt"
                ) {

                    onProgress(
                        "Leyendo datos de Pidem"
                    )

                    val reader =
                        BufferedReader(
                            InputStreamReader(
                                zip,
                                Charsets.UTF_8
                            )
                        )

                    var line = reader.readLine()

                    while (line != null) {

                        when {

                            line == EXPORT_VERSION -> {

                                versionFound = true
                            }

                            line == "CATEGORIES" -> {

                                currentSection = "CATEGORIES"
                            }

                            line == "PHOTOS" -> {

                                currentSection = "PHOTOS"
                            }

                            line == "ATTACHMENTS" -> {

                                currentSection = "ATTACHMENTS"
                            }

                            line.isBlank() -> {
                            }

                            currentSection == "CATEGORIES" -> {

                                val fields =
                                    splitFields(line)

                                if (fields.size >= 2) {

                                    categories.add(
                                        ImportedCategory(

                                            oldId =
                                                fields[0].toLong(),

                                            name =
                                                fields[1]
                                        )
                                    )
                                }
                            }

                            currentSection == "PHOTOS" -> {

                                val fields =
                                    splitFields(line)

                                if (fields.size >= 7) {

                                    photos.add(
                                        ImportedPhoto(

                                            oldId =
                                                fields[0].toLong(),

                                            firstPhotoEntry =
                                                "images/" +
                                                        fields[0] +
                                                        "_first.jpg",

                                            secondPhotoEntry =
                                                if (
                                                    fields[2].isBlank()
                                                ) {
                                                    null
                                                } else {
                                                    "images/" +
                                                            fields[0] +
                                                            "_second.jpg"
                                                },

                                            ocrText =
                                                fields[3],

                                            additionalText =
                                                fields[4],

                                            category =
                                                fields[5],

                                            createdAt =
                                                fields[6].toLong()
                                        )
                                    )
                                }
                            }

                            currentSection == "ATTACHMENTS" -> {

                                val fields =
                                    splitFields(line)

                                if (fields.size >= 4) {

                                    attachments.add(
                                        ImportedAttachment(

                                            oldId =
                                                fields[0].toLong(),

                                            oldPhotoId =
                                                fields[1].toLong(),

                                            imageEntry =
                                                "attachments/" +
                                                        fields[0] +
                                                        ".jpg",

                                            createdAt =
                                                fields[3].toLong()
                                        )
                                    )
                                }
                            }
                        }

                        line = reader.readLine()
                    }


                }


                zip.closeEntry()

                entry =
                    zip.nextEntry
            }


            if (!versionFound) {

                throw Exception(
                    "El archivo no contiene una versión de exportación válida"
                )
            }
        }


        onProgress(
            "Datos leídos: ${photos.size} fotografías"
        )


        return ImportedData(
            categories = categories,
            photos = photos,
            attachments = attachments
        )
    }


    /*
     * =============================================================
     * SEPARAR CAMPOS
     * =============================================================
     */

    private fun splitFields(
        line: String
    ): List<String> {

        return line
            .split(FIELD_SEPARATOR)
            .map {
                unescape(it)
            }
    }


    /*
     * =============================================================
     * DES-ESCAPAR
     * =============================================================
     */

    private fun unescape(
        value: String
    ): String {

        val result =
            StringBuilder()

        var escaped = false
        var index = 0

        while (index < value.length) {

            val char =
                value[index]

            if (escaped) {

                when (char) {

                    'n' ->
                        result.append('\n')

                    'r' ->
                        result.append('\r')

                    '\\' ->
                        result.append('\\')

                    else -> {

                        result.append('\\')
                        result.append(char)
                    }
                }

                escaped = false

            } else {

                if (char == '\\') {

                    escaped = true

                } else {

                    result.append(char)
                }
            }

            index++
        }


        if (escaped) {

            result.append('\\')
        }


        return result.toString()
    }


    /*
     * =============================================================
     * EXTRAER TODAS LAS IMÁGENES
     * =============================================================
     */

    fun extractAllImages(
        context: Context,
        fileUri: Uri,
        data: ImportedData,
        onProgress: (String) -> Unit = {}
    ): Result<
            Triple<
                    Map<Long, String>,
                    Map<Long, String>,
                    Map<Long, String>
                    >
            > {

        return try {

            val firstPhotos =
                mutableMapOf<Long, String>()

            val secondPhotos =
                mutableMapOf<Long, String>()

            val attachments =
                mutableMapOf<Long, String>()


            val storageUri =
                PidemStorage.getStorageUri(
                    context
                )
                    ?: throw Exception(
                        "No existe una carpeta de almacenamiento autorizada"
                    )


            val selectedFolder =
                DocumentFile.fromTreeUri(
                    context,
                    storageUri
                )
                    ?: throw Exception(
                        "No se pudo acceder a la carpeta de almacenamiento"
                    )


            var pidemFolder =
                selectedFolder.findFile(
                    "Pidem"
                )


            if (pidemFolder == null) {

                pidemFolder =
                    selectedFolder.createDirectory(
                        "Pidem"
                    )
            }


            pidemFolder
                ?: throw Exception(
                    "No se pudo crear la carpeta Pidem"
                )


            val inputStream =
                context.contentResolver.openInputStream(
                    fileUri
                )
                    ?: throw Exception(
                        "No se pudo abrir el archivo .eiP"
                    )


            ZipInputStream(
                BufferedInputStream(
                    inputStream
                )
            ).use { zip ->

                var entry =
                    zip.nextEntry


                while (entry != null) {

                    if (
                        !entry.isDirectory &&
                        (
                                entry.name.startsWith("images/") ||
                                        entry.name.startsWith("attachments/")
                                )
                    ) {

                        val fileName =
                            entry.name.substringAfterLast("/")


                        val existing =
                            pidemFolder.findFile(
                                fileName
                            )

                        existing?.delete()


                        val destination =
                            pidemFolder.createFile(
                                "image/jpeg",
                                fileName
                            )
                                ?: throw Exception(
                                    "No se pudo crear $fileName"
                                )


                        val outputStream =
                            context.contentResolver.openOutputStream(
                                destination.uri
                            )
                                ?: throw Exception(
                                    "No se pudo abrir $fileName"
                                )


                        outputStream.use { output ->

                            val buffer =
                                ByteArray(8192)

                            while (true) {

                                val count =
                                    zip.read(buffer)

                                if (count == -1) {
                                    break
                                }

                                output.write(
                                    buffer,
                                    0,
                                    count
                                )
                            }
                        }


                        /*
                         * IMÁGENES PRINCIPALES
                         */

                        if (
                            entry.name.startsWith("images/")
                        ) {

                            val photoId =
                                entry.name
                                    .substringAfter("images/")
                                    .substringBefore("_")
                                    .toLong()


                            if (
                                entry.name.endsWith("_first.jpg")
                            ) {

                                firstPhotos[photoId] =
                                    destination.uri.toString()

                            } else if (
                                entry.name.endsWith("_second.jpg")
                            ) {

                                secondPhotos[photoId] =
                                    destination.uri.toString()
                            }


                        } else {

                            /*
                             * ADJUNTOS
                             */

                            val attachmentId =
                                entry.name
                                    .substringAfter("attachments/")
                                    .substringBefore(".")
                                    .toLong()


                            attachments[attachmentId] =
                                destination.uri.toString()
                        }
                    }


                    zip.closeEntry()

                    entry =
                        zip.nextEntry
                }
            }


            onProgress(
                "Imágenes extraídas"
            )


            Result.success(
                Triple(
                    firstPhotos,
                    secondPhotos,
                    attachments
                )
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}


