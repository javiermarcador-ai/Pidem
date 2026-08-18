package es.fotoindex.app.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import es.fotoindex.app.data.PidemStorage
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import android.provider.MediaStore
import androidx.annotation.RequiresApi

object CameraPreview {

    private var imageCapture: ImageCapture? = null

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()

            val imageCapture =
                ImageCapture.Builder().build()

            this.imageCapture = imageCapture

            preview.surfaceProvider =
                previewView.surfaceProvider

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(context))
    }

    fun copyGalleryImage(
        context: Context,
        uri: Uri,
        onCopied: (String) -> Unit
    ) {

        val input =
            context.contentResolver.openInputStream(uri)
                ?: return

        val fileName =
            "gallery_" +
                    System.currentTimeMillis() +
                    ".jpg"

        val destination =
            PidemStorage.createImageFile(
                context,
                fileName
            )

        if (destination == null) {

            input.close()

            Toast.makeText(
                context,
                "No se ha seleccionado una carpeta de almacenamiento",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val output =
            context.contentResolver.openOutputStream(
                destination.uri
            )

        if (output == null) {

            input.close()

            Toast.makeText(
                context,
                "No se pudo guardar la fotografía",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        input.copyTo(output)

        input.close()
        output.close()

        onCopied(
            destination.uri.toString()
        )
    }

    fun takePhoto(
        context: Context,
        onPhotoSaved: (String) -> Unit
    ) {

        val imageCapture =
            imageCapture ?: return

        val fileName =
            "Pidem_" +
                    SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(
                        System.currentTimeMillis()
                    ) +
                    ".jpg"

        val destination =
            PidemStorage.createImageFile(
                context,
                fileName
            )

        if (destination == null) {

            Toast.makeText(
                context,
                "No se ha seleccionado una carpeta de almacenamiento",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val outputStream =
            context.contentResolver.openOutputStream(
                destination.uri
            )

        if (outputStream == null) {

            Toast.makeText(
                context,
                "No se pudo crear la fotografía",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val tempFile =
            java.io.File.createTempFile(
                "pidem_capture_",
                ".jpg"
            )

        val outputOptions =
            ImageCapture.OutputFileOptions
                .Builder(tempFile)
                .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object :
                ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults:
                    ImageCapture.OutputFileResults
                ) {

                    try {

                        FileInputStream(
                            tempFile
                        ).use { input ->

                            input.copyTo(
                                outputStream
                            )
                        }

                        outputStream.close()

                        tempFile.delete()

                        Toast.makeText(
                            context,
                            "Foto guardada",
                            Toast.LENGTH_SHORT
                        ).show()

                        onPhotoSaved(
                            destination.uri.toString()
                        )

                    } catch (e: Exception) {

                        outputStream.close()

                        tempFile.delete()

                        Toast.makeText(
                            context,
                            "Error al guardar la fotografía",
                            Toast.LENGTH_LONG
                        ).show()

                        e.printStackTrace()
                    }
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    outputStream.close()

                    tempFile.delete()

                    Toast.makeText(
                        context,
                        exception.message
                            ?: "Error desconocido",
                        Toast.LENGTH_LONG
                    ).show()

                    exception.printStackTrace()
                }
            }
        )
    }

    fun deleteImage(
        context: Context,
        path: String
    ): Boolean {
        return try {
            val uri = Uri.parse(path)
            /*
             * Las imágenes de Pidem se guardan mediante
             * Storage Access Framework (DocumentFile).
             *
             * Por tanto, para eliminarlas físicamente
             * debemos utilizar DocumentFile.delete().
             */
            val documentFile =
                DocumentFile.fromSingleUri(
                    context,
                    uri
                )
            if (documentFile != null) {          documentFile.delete()
            } else {
                /*      Fallback para URI que pueda manejar       * directamente el ContentResolver.  */
                context.contentResolver.delete(
                    uri,
                    null,
                    null
                ) > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun deleteOriginalImage(
        context: Context,
        path: String
    ): Boolean {

        return try {

            val uri = Uri.parse(path)

            if (
                uri.authority ==
                "com.android.externalstorage.documents"
            ) {

                val documentFile =
                    DocumentFile.fromSingleUri(
                        context,
                        uri
                    )

                documentFile?.delete() == true

            } else {

                /*
                 * Las imágenes procedentes de Gallery /
                 * Photo Picker no se pueden eliminar
                 * directamente desde aquí.
                 *
                 * El borrado se gestionará posteriormente
                 * mediante la autorización de Android.
                 */
                false
            }

        } catch (e: Exception) {

            e.printStackTrace()

            false
        }
    }

    fun isGalleryImage(
        path: String
    ): Boolean {

        val uri = Uri.parse(path)

        return uri.authority == "media"
    }


    @RequiresApi(30)
    fun createGalleryDeleteRequest(
        context: Context,
        paths: List<String>
    ): android.app.PendingIntent? {

        val resolver = context.contentResolver

        val mediaStoreUris =
            paths
                .mapNotNull { path ->

                    try {

                        val uri = Uri.parse(path)

                        /*
                         * Las imágenes seleccionadas desde Gallery
                         * mediante GetContent() pueden llegar como:
                         *
                         * content://media/picker_get_content/0/...
                         * /media/1000403662
                         *
                         * El último segmento es el ID de MediaStore.
                         */

                        if (uri.authority != MediaStore.AUTHORITY) {
                            return@mapNotNull null
                        }

                        val lastSegment =
                            uri.lastPathSegment
                                ?: return@mapNotNull null

                        /*
                         * Por si el proveedor devuelve el ID
                         * acompañado de una extensión.
                         */
                        val idString =
                            lastSegment.substringBefore(".")

                        val id =
                            idString.toLongOrNull()
                                ?: return@mapNotNull null

                        /*
                         * Creamos la URI real de MediaStore
                         * que identifica exactamente la imagen.
                         */
                        MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY,
                            id
                        )

                    } catch (e: Exception) {

                        e.printStackTrace()
                        null
                    }
                }
                .distinct()

        if (mediaStoreUris.isEmpty()) {
            return null
        }

        return try {

            MediaStore.createDeleteRequest(
                resolver,
                mediaStoreUris
            )

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }





}