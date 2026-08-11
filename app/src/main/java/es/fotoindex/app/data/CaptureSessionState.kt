package es.fotoindex.app.data

object CaptureSessionState {

    var firstOcrText = ""

    var secondOcrText = ""

    var firstPhotoFromGallery = false

    var additionalPhotoDocumentId: Long? = null

    fun clear() {

        firstOcrText = ""

        secondOcrText = ""

        firstPhotoFromGallery = false

        additionalPhotoDocumentId = null

    }
}