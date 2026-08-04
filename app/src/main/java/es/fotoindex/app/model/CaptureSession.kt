
package es.fotoindex.app.model

import es.fotoindex.app.model.CaptureSource

data class CaptureSession(

    val firstPhotoPath: String? = null,
    val secondPhotoPath: String? = null,
    val previewPhotoPath: String? = null,

    val firstOcrText: String = "",
    val secondOcrText: String = "",

    val reviewingPhoto: Boolean = false,
    val reviewingFirstPhoto: Boolean = true,

    val showSecondPhotoDialog: Boolean = false,

    val source: CaptureSource = CaptureSource.CAMERA,

    val ocrText: String = ""

)