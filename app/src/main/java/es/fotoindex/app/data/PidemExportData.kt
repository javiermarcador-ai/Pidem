package es.fotoindex.app.data

import es.fotoindex.app.database.Category
import es.fotoindex.app.database.PhotoAttachment
import es.fotoindex.app.database.PhotoRecord

data class PidemExportData(

    val photos: List<PhotoRecord>,

    val attachments: List<PhotoAttachment>,

    val categories: List<Category>

)


