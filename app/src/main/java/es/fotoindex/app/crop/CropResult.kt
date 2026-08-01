package es.fotoindex.app.crop

sealed class CropResult {

    data class Accepted(

        val imagePath: String

    ) : CropResult()

    data object Cancelled : CropResult()

}