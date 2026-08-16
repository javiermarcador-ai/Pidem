package es.fotoindex.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryRecord(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String

)

