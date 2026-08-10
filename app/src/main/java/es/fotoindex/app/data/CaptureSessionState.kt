package es.fotoindex.app.data

object CaptureSessionState {

    var firstOcrText = ""

    var secondOcrText = ""

    var firstPhotoFromGallery = false

    fun clear() {

        firstOcrText = ""

        secondOcrText = ""

        firstPhotoFromGallery = false

    }
}