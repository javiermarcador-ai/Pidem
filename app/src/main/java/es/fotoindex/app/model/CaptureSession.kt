
package es.fotoindex.app.model


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

    val ocrText: String = "",

    val category: String = "Documentos",


    // Imágenes originales del usuario.
    // Se utilizarán posteriormente para ofrecer
    // su eliminación después de guardar en Pidem.
    val originalImagePaths: List<String> = emptyList(),

    // Copia temporal utilizada para procesar
    // una imagen procedente de la galería.
    // Se eliminará automáticamente después
    // de generar la imagen definitiva de Pidem.
    val temporaryGalleryPath: String? = null

)