package es.fotoindex.app.ocr

object OcrCleaner {

    fun clean(text: String): String {

        return text

            .replace(Regex("[ \t]+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

    }

}