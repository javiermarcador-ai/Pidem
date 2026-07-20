package es.fotoindex.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val imageUri: String,

    val extractedText: String,

    val category: String,

    val parentId: Long? = null,

    val createdAt: Long = System.currentTimeMillis()

)