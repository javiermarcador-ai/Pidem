package es.fotoindex.app.crop

import android.content.Context

object CropManager {

    fun cropPhoto(

        context: Context,

        photoPath: String,

        onResult: (CropResult) -> Unit

    ) {

        // De momento siempre aceptamos la foto.
        // Aquí conectaremos el recortador.

        onResult(

            CropResult(

                imagePath = photoPath,

                accepted = true

            )

        )

    }

}