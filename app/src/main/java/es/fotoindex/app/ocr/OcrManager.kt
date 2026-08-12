package es.fotoindex.app.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrManager {

    fun extractText(

        context: Context,

        imagePath: String,

        onSuccess: (String) -> Unit,

        onError: (Exception) -> Unit

    ) {

        try {

            val uri =
                Uri.parse(imagePath)

            val image =
                InputImage.fromFilePath(
                    context,
                    uri
                )

            val recognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
                )

            recognizer.process(image)

                .addOnSuccessListener { visionText ->

                    onSuccess(
                        visionText.text
                    )

                }

                .addOnFailureListener {

                    onError(it)

                }

        } catch (e: Exception) {

            onError(e)

        }

    }
}