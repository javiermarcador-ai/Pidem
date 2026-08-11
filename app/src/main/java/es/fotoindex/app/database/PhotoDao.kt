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
ORDER BY createdAt DESC
""")
    suspend fun search(text: String): List<PhotoRecord>

    @Query("""
SELECT *
FROM photos
WHERE
ocrText LIKE '%' || :text || '%'
OR
additionalText LIKE '%' || :text || '%'
ORDER BY createdAt DESC
""")
    suspend fun searchIncludingNotes(text: String): List<PhotoRecord>



    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
UPDATE photos
SET additionalText = :notes
WHERE id = :id
""")
    suspend fun updateNotes(
        id: Long,
        notes: String
    )

    @Insert
    suspend fun insertAttachment(
        attachment: PhotoAttachment
    )

    @Query("""
SELECT *
FROM photo_attachments
WHERE photoId = :photoId
ORDER BY createdAt ASC
""")
    suspend fun getAttachments(
        photoId: Long
    ): List<PhotoAttachment>

    @Query("""
DELETE FROM photo_attachments
WHERE id = :attachmentId
""")
    suspend fun deleteAttachment(
        attachmentId: Long
    )


}