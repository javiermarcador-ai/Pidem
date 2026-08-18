package es.fotoindex.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_attachments")
data class PhotoAttachment(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val photoId: Long,

    val imagePath: String,

    val createdAt: Long = System.currentTimeMillis()

)


