package es.fotoindex.app.data

object CaptureSessionState {

    var firstOcrText = ""

    var secondOcrText = ""

    fun clear() {
        firstOcrText = ""
        secondOcrText = ""
    }
}