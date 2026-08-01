package es.fotoindex.app.crop

data class CropRequest(

    val imagePath: String,

    val aspectRatioX: Int = 0,

    val aspectRatioY: Int = 0,

    val fixAspectRatio: Boolean = false

)
