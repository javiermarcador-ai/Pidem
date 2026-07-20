package es.fotoindex.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: Photo): Long

    @Update
    suspend fun update(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getById(id: Long): Photo?

    @Query("""
        SELECT * FROM photos
        WHERE extractedText LIKE '%' || :text || '%'
        ORDER BY createdAt DESC
    """)
    fun search(text: String): Flow<List<Photo>>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}