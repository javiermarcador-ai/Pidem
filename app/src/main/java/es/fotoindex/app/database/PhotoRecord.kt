package es.fotoindex.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoRecord(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val firstPhoto: String,

    val secondPhoto: String?,

    val ocrText: String,

    val additionalText: String,

    val category: String = "Documentos",

    val createdAt: Long

)

