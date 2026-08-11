package es.fotoindex.app.export

data class LgdExport(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val documents: List<LgdDocument>
)

data class LgdDocument(
    val firstPhoto: String,
    val secondPhoto: String?,
    val ocrText: String,
    val additionalText: String,
    val createdAt: Long
)

