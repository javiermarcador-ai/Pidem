package es.fotoindex.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import es.fotoindex.app.ui.CropArea
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

object ImageCropper {

    fun loadBitmap(path: String): Bitmap {

        val original = BitmapFactory.decodeFile(path)

        val exif = ExifInterface(path)

        val orientation = exif.getAttributeInt(

            ExifInterface.TAG_ORIENTATION,

            ExifInterface.ORIENTATION_NORMAL

        )

        val matrix = Matrix()

        when (orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 ->
                matrix.postRotate(90f)

            ExifInterface.ORIENTATION_ROTATE_180 ->
                matrix.postRotate(180f)

            ExifInterface.ORIENTATION_ROTATE_270 ->
                matrix.postRotate(270f)

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
            bitmap.width / (imageRight - imageLeft)

        val scaleY =
            bitmap.height / (imageBottom - imageTop)

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

        return Bitmap.createBitmap(

            bitmap,

            x.coerceAtLeast(0),

            y.coerceAtLeast(0),

            width.coerceAtMost(bitmap.width - x),

            height.coerceAtMost(bitmap.height - y)

        )

    }

    fun saveBitmap(

        context: Context,

        bitmap: Bitmap

    ): String {

        val dir = File(
            context.getExternalFilesDir("Pictures"),

            "LgDragon"
        )

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val today = java.text.SimpleDateFormat(

            "yyyyMMdd",

            java.util.Locale.getDefault()

        ).format(java.util.Date())

        val existing = dir.listFiles()

            ?.map { it.name }

            ?.filter {

                it.startsWith("lgDrag_$today")

            }

            ?.size ?: 0

        val number = String.format(

            "%03d",

            existing + 1

        )

        val file = File(

            dir,

            "lgDrag_${today}_$number.jpg"

        )

        FileOutputStream(file).use {

            bitmap.compress(

                Bitmap.CompressFormat.JPEG,

                100,

                it

            )

        }

        return file.absolutePath

    }

    fun processCrop(

    context: Context,

    bitmap: Bitmap,

    cropArea: CropArea

    ): String {

        val croppedBitmap = cropBitmap(

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