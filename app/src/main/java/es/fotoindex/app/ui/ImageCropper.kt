package es.fotoindex.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import es.fotoindex.app.data.PidemStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object ImageCropper {

    fun loadBitmap(
        context: Context,
        path: String
    ): Bitmap {

        val uri = Uri.parse(path)

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException(
                    "No se pudo abrir la imagen"
                )

        val original = inputStream.use {

            BitmapFactory.decodeStream(it)
                ?: throw IllegalStateException(
                    "No se pudo decodificar la imagen"
                )

        }

        /*
         * Normalizamos la orientación UNA SOLA VEZ.
         *
         * A partir de aquí Pidem trabaja únicamente
         * con los píxeles ya orientados.
         */

        val exifInputStream =
            context.contentResolver.openInputStream(uri)

        val orientation =
            exifInputStream?.use {

                val exif = ExifInterface(it)

                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

            } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()

        when (orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
            }

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                matrix.setScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setScale(1f, -1f)
            }

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }

        if (matrix.isIdentity) {

            return original

        }

        return Bitmap.createBitmap(
            original,
            0,
            0,
            original.width,
            original.height,
            matrix,
            true
        )
    }

    fun cropBitmap(

        bitmap: Bitmap,

        imageLeft: Float,
        imageTop: Float,
        imageRight: Float,
        imageBottom: Float,

        cropLeft: Float,
        cropTop: Float,
        cropRight: Float,
        cropBottom: Float

    ): Bitmap {

        val scaleX =
            bitmap.width /
                    (imageRight - imageLeft)

        val scaleY =
            bitmap.height /
                    (imageBottom - imageTop)

        val x =
            ((cropLeft - imageLeft) * scaleX)
                .roundToInt()

        val y =
            ((cropTop - imageTop) * scaleY)
                .roundToInt()

        val width =
            ((cropRight - cropLeft) * scaleX)
                .roundToInt()

        val height =
            ((cropBottom - cropTop) * scaleY)
                .roundToInt()

        val safeX =
            x.coerceIn(
                0,
                bitmap.width - 1
            )

        val safeY =
            y.coerceIn(
                0,
                bitmap.height - 1
            )

        val safeWidth =
            width.coerceIn(
                1,
                bitmap.width - safeX
            )

        val safeHeight =
            height.coerceIn(
                1,
                bitmap.height - safeY
            )

        return Bitmap.createBitmap(
            bitmap,
            safeX,
            safeY,
            safeWidth,
            safeHeight
        )
    }


    fun saveBitmap(
        context: Context,
        bitmap: Bitmap
    ): String {

        val today =
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())

        val fileName =
            "Pidem_${today}_" +
                    System.currentTimeMillis() +
                    ".jpg"

        val destination =
            PidemStorage.createImageFile(
                context,
                fileName
            )
                ?: throw IllegalStateException(
                    "No se ha seleccionado una carpeta de almacenamiento"
                )

        context.contentResolver
            .openOutputStream(destination.uri)
            ?.use { output ->

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    output
                )

            }
            ?: throw IllegalStateException(
                "No se pudo guardar la fotografía"
            )

        return destination.uri.toString()
    }


    fun processCrop(

        context: Context,

        bitmap: Bitmap,

        cropArea: CropArea

    ): String {

        val croppedBitmap =
            cropBitmap(

                bitmap = bitmap,

                imageLeft = cropArea.imageLeft,
                imageTop = cropArea.imageTop,
                imageRight = cropArea.imageRight,
                imageBottom = cropArea.imageBottom,

                cropLeft = cropArea.cropLeft,
                cropTop = cropArea.cropTop,
                cropRight = cropArea.cropRight,
                cropBottom = cropArea.cropBottom

            )

        return saveBitmap(
            context,
            croppedBitmap
        )
    }
}