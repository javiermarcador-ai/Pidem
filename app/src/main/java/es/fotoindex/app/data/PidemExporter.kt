package es.fotoindex.app.data

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import es.fotoindex.app.database.Category
import es.fotoindex.app.database.PhotoAttachment
import es.fotoindex.app.database.PhotoRecord
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PidemExporter {

    private const val EXPORT_EXTENSION = ".eiP"

    /*
     * Separador único de campos.
     *
     * Se utiliza para separar TODOS los campos
     * de categorías, fotografías y adjuntos.
     */
    private const val FIELD_SEPARATOR =
        "/$3p4r4t*rYúnke"

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun export(
        context: Context,
        photos: List<PhotoRecord>,
        attachments: List<PhotoAttachment>,
        categories: List<Category>
    ): Result<String> {

        return try {

            /*
             * Nombre:
             *
             * aaaammddhhmm_datosPidem.eiP
             */
            val dateFormat =
                SimpleDateFormat(
                    "yyyyMMddHHmm",
                    Locale.getDefault()
                )

            val dateTime =
                dateFormat.format(Date())

            val fileName =
                "${dateTime}_datosPidem$EXPORT_EXTENSION"

            /*
             * =====================================================
             * CREAR ARCHIVO EN DOWNLOAD
             * =====================================================
             */

            val resolver =
                context.contentResolver

            val values =
                android.content.ContentValues().apply {

                    put(
                        android.provider.MediaStore.Downloads.DISPLAY_NAME,
                        fileName
                    )

                    put(
                        android.provider.MediaStore.Downloads.MIME_TYPE,
                        "application/octet-stream"
                    )

                    put(
                        android.provider.MediaStore.Downloads.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )

                    put(
                        android.provider.MediaStore.Downloads.IS_PENDING,
                        1
                    )
                }

            val exportUri =
                resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                    ?: return Result.failure(
                        Exception(
                            "Android no pudo crear el archivo en Download"
                        )
                    )

            val outputStream =
                resolver.openOutputStream(
                    exportUri
                )
                    ?: return Result.failure(
                        Exception(
                            "No se pudo abrir el archivo de exportación"
                        )
                    )

            ZipOutputStream(
                BufferedOutputStream(
                    outputStream
                )
            ).use { zip ->

                /*
                 * =====================================================
                 * DATOS DE PIDEM
                 * =====================================================
                 */

                val dataEntry =
                    ZipEntry(
                        "pidem_data.txt"
                    )

                zip.putNextEntry(dataEntry)

                /*
                 * Versión del formato de exportación.
                 */
                zip.write(
                    "PIDEM_EXPORT_VERSION=1\n"
                        .toByteArray(Charsets.UTF_8)
                )

                /*
                 * =====================================================
                 * CATEGORÍAS
                 * =====================================================
                 */

                zip.write(
                    "CATEGORIES\n"
                        .toByteArray(Charsets.UTF_8)
                )

                categories.forEach { category ->

                    zip.write(
                        (
                                "${category.id}" +
                                        FIELD_SEPARATOR +
                                        escape(category.name) +
                                        "\n"
                                ).toByteArray(
                                Charsets.UTF_8
                            )
                    )
                }

                /*
                 * =====================================================
                 * FOTOGRAFÍAS
                 * =====================================================
                 */

                zip.write(
                    "PHOTOS\n"
                        .toByteArray(Charsets.UTF_8)
                )

                photos.forEach { photo ->

                    /*
                     * IMPORTANTE:
                     *
                     * El OCR y las notas pueden contener
                     * saltos de línea.
                     *
                     * El separador FIELD_SEPARATOR es el
                     * delimitador real de los campos.
                     */

                    zip.write(
                        (
                                "${photo.id}" +
                                        FIELD_SEPARATOR +
                                        escape(photo.firstPhoto) +
                                        FIELD_SEPARATOR +
                                        escape(photo.secondPhoto ?: "") +
                                        FIELD_SEPARATOR +
                                        escape(photo.ocrText) +
                                        FIELD_SEPARATOR +
                                        escape(photo.additionalText) +
                                        FIELD_SEPARATOR +
                                        escape(photo.category) +
                                        FIELD_SEPARATOR +
                                        "${photo.createdAt}" +
                                        "\n"
                                ).toByteArray(
                                Charsets.UTF_8
                            )
                    )
                }

                /*
                 * =====================================================
                 * ADJUNTOS
                 * =====================================================
                 */

                zip.write(
                    "ATTACHMENTS\n"
                        .toByteArray(Charsets.UTF_8)
                )

                attachments.forEach { attachment ->

                    zip.write(
                        (
                                "${attachment.id}" +
                                        FIELD_SEPARATOR +
                                        "${attachment.photoId}" +
                                        FIELD_SEPARATOR +
                                        escape(attachment.imagePath) +
                                        FIELD_SEPARATOR +
                                        "${attachment.createdAt}" +
                                        "\n"
                                ).toByteArray(
                                Charsets.UTF_8
                            )
                    )
                }

                zip.closeEntry()

                /*
                 * =====================================================
                 * IMÁGENES PRINCIPALES
                 * =====================================================
                 */

                photos.forEach { photo ->

                    addImageToZip(
                        context = context,
                        zip = zip,
                        imagePath = photo.firstPhoto,
                        zipPath =
                            "images/${photo.id}_first.jpg"
                    )

                    photo.secondPhoto?.let { secondPhoto ->

                        addImageToZip(
                            context = context,
                            zip = zip,
                            imagePath = secondPhoto,
                            zipPath =
                                "images/${photo.id}_second.jpg"
                        )
                    }
                }

                /*
                 * =====================================================
                 * IMÁGENES ADJUNTAS
                 * =====================================================
                 */

                attachments.forEach { attachment ->

                    addImageToZip(
                        context = context,
                        zip = zip,
                        imagePath = attachment.imagePath,
                        zipPath =
                            "attachments/" +
                                    "${attachment.id}.jpg"
                    )
                }
            }

            /*
             * El archivo ya está terminado.
             * Lo hacemos visible en Download.
             */

            resolver.update(
                exportUri,
                android.content.ContentValues().apply {
                    put(
                        android.provider.MediaStore.Downloads.IS_PENDING,
                        0
                    )
                },
                null,
                null
            )

            Result.success(
                fileName
            )

        } catch (e: Exception) {

            Result.failure(e)

        }
    }


    /*
     * =============================================================
     * AÑADIR IMAGEN AL ZIP
     * =============================================================
     */

    private fun addImageToZip(
        context: Context,
        zip: ZipOutputStream,
        imagePath: String,
        zipPath: String
    ) {

        val uri =
            Uri.parse(imagePath)

        val inputStream =
            context.contentResolver.openInputStream(
                uri
            )
                ?: return

        inputStream.use { input ->

            zip.putNextEntry(
                ZipEntry(zipPath)
            )

            BufferedInputStream(
                input
            ).use { bufferedInput ->

                val buffer =
                    ByteArray(
                        8192
                    )

                while (true) {

                    val count =
                        bufferedInput.read(
                            buffer
                        )

                    if (count == -1) {
                        break
                    }

                    zip.write(
                        buffer,
                        0,
                        count
                    )
                }
            }

            zip.closeEntry()
        }
    }


    /*
     * =============================================================
     * ESCAPE
     * =============================================================
     *
     * Conservamos este método para proteger las URI y los textos
     * de los caracteres especiales que puedan interferir con el
     * formato.
     *
     * El carácter "|" ya no es nuestro separador principal,
     * pero mantener esta protección no perjudica la importación.
     */

    private fun escape(
        value: String
    ): String {

        return value
            .replace(
                "\\",
                "\\\\"
            )
            .replace(
                "\n",
                "\\n"
            )
            .replace(
                "\r",
                "\\r"
            )
    }
}


