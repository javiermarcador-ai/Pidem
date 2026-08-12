package es.fotoindex.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import androidx.compose.ui.platform.LocalContext


@Composable
fun CropImageView(

    imagePath: String,

    cropArea: CropArea

) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val bitmap = remember(imagePath) {

        ImageCropper.loadBitmap(
            context,
            imagePath
        )

    }

    CropBitmapState.bitmap = bitmap

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    var left by remember { mutableStateOf(0f) }
    var top by remember { mutableStateOf(0f) }
    var right by remember { mutableStateOf(-1f) }
    var bottom by remember { mutableStateOf(-1f) }
    var imageLeft by remember { mutableStateOf(0f) }
    var imageTop by remember { mutableStateOf(0f) }
    var imageRight by remember { mutableStateOf(0f) }
    var imageBottom by remember { mutableStateOf(0f) }


    Box(

        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {

                detectDragGestures(

                    onDrag = { change, drag ->

                        val tolerance = 120f

                        val x = change.position.x
                        val y = change.position.y

                        if (x <= left + tolerance) {

                            left = (left + drag.x)
                                .coerceAtLeast(imageLeft)
                                .coerceAtMost(right - 120f)

                        }

                        if (x >= right - tolerance) {

                            right = (right + drag.x)
                                .coerceAtLeast(left + 120f)
                                .coerceAtMost(imageRight)

                        }

                        if (y <= top + tolerance) {

                            top = (top + drag.y)
                                .coerceAtLeast(imageTop)
                                .coerceAtMost(bottom - 120f)

                        }

                        if (y >= bottom - tolerance) {

                            bottom = (bottom + drag.y)
                                .coerceAtLeast(top + 120f)
                                .coerceAtMost(imageBottom)

                        }

                    }

                )

            }

    ) {

        Image(

            bitmap = bitmap.asImageBitmap(),

            contentDescription = null,

            modifier = Modifier.fillMaxSize(),

            contentScale = ContentScale.Fit

        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            canvasWidth = size.width
            canvasHeight = size.height

            val imageRatio =
                bitmap.width.toFloat() / bitmap.height.toFloat()

            val canvasRatio =
                size.width / size.height

            if (imageRatio > canvasRatio) {

                val displayedHeight = size.width / imageRatio

                imageLeft = 0f
                imageRight = size.width

                imageTop = (size.height - displayedHeight) / 2f
                imageBottom = imageTop + displayedHeight

            } else {

                val displayedWidth = size.height * imageRatio

                imageTop = 0f
                imageBottom = size.height

                imageLeft = (size.width - displayedWidth) / 2f
                imageRight = imageLeft + displayedWidth

            }

            if (right < 0f) {

                left = imageLeft
                top = imageTop
                right = imageRight
                bottom = imageBottom

            }

            drawRect(

                color = Color.Black.copy(alpha = 0.45f),

                topLeft = Offset(0f, 0f),

                size = Size(size.width, top)

            )

            drawRect(

                color = Color.Black.copy(alpha = 0.45f),

                topLeft = Offset(0f, bottom),

                size = Size(size.width, size.height - bottom)

            )

            drawRect(

                color = Color.Black.copy(alpha = 0.45f),

                topLeft = Offset(0f, top),

                size = Size(left, bottom - top)

            )

            drawRect(

                color = Color.Black.copy(alpha = 0.45f),

                topLeft = Offset(right, top),

                size = Size(size.width - right, bottom - top)

            )

            drawRect(

                color = Color.LightGray,

                topLeft = Offset(left, top),

                size = Size(right - left, bottom - top),

                style = Stroke(width = 3.dp.toPx())

            )

            cropArea.imageLeft = imageLeft
            cropArea.imageTop = imageTop
            cropArea.imageRight = imageRight
            cropArea.imageBottom = imageBottom

            cropArea.cropLeft = left
            cropArea.cropTop = top
            cropArea.cropRight = right
            cropArea.cropBottom = bottom

        }

    }

}