package es.fotoindex.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: PhotoRecord)

    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    suspend fun getAll(): List<PhotoRecord>

}