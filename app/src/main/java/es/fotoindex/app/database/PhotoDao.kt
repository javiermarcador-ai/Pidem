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

    @Query("""
SELECT *
FROM photos
WHERE
ocrText LIKE '%' || :text || '%'
OR
additionalText LIKE '%' || :text || '%'
ORDER BY createdAt DESC
""")
    suspend fun search(text: String): List<PhotoRecord>



    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)


}