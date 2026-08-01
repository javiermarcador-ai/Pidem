package es.fotoindex.app.ocr

import android.content.Context

object OcrPipeline {

    fun process(

        context: Context,

        imagePath: String,

        onSuccess: (String) -> Unit,

        onError: (Exception) -> Unit

    ) {

        OcrManager.extractText(

            context = context,

            imagePath = imagePath,

            onSuccess = onSuccess,

            onError = onError

        )

    }

}