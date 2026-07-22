package es.fotoindex.app.ui



data class CameraUiState(

    val firstPhotoPath: String? = null,

    val secondPhotoPath: String? = null,

    val previewPhotoPath: String? = null,

    val reviewingPhoto: Boolean = false,

    val reviewingFirstPhoto: Boolean = true,

    val showSecondPhotoDialog: Boolean = false

)