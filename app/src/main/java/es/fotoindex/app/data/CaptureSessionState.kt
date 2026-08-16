package es.fotoindex.app.data

object CaptureSessionState {

    var firstOcrText = ""

    var secondOcrText = ""

    var firstPhotoFromGallery = false

    var additionalPhotoDocumentId: Long? = null

    // Categoría seleccionada en HomeScreen
    var selectedCategory = "Documentos"

    fun clear() {

        firstOcrText = ""

        secondOcrText = ""

        firstPhotoFromGallery = false

        additionalPhotoDocumentId = null

        selectedCategory = "Documentos"
    }
}