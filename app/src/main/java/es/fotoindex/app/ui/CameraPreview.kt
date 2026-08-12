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
            kotlin.io.path.createTempFile(
                prefix = "pidem_capture_",
                suffix = ".jpg"
            ).toFile()

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
}