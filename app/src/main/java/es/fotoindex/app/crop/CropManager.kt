package es.fotoindex.app.crop

import android.content.Context
import android.net.Uri
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions


object CropManager {

    fun cropPhoto(

        context: Context,

        request: CropRequest,

        onResult: (CropResult) -> Unit

    ) {

        // De momento siempre aceptamos la foto.
        // Aquí conectaremos el recortador.

        onResult(

            CropResult.Accepted(

                imagePath = request.imagePath

            )

        )

    }

}