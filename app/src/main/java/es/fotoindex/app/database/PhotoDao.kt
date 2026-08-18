package es.fotoindex.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(
        photo: PhotoRecord
    )


    @Query("""
        SELECT *
        FROM photos
        ORDER BY createdAt DESC
    """)
    suspend fun getAll(): List<PhotoRecord>



    @Query("""
        SELECT *
        FROM photos
        WHERE category = :category
        ORDER BY createdAt DESC
    """)
    suspend fun getByCategory(
        category: String
    ): List<PhotoRecord>


    @Query("""
    SELECT *
    FROM photos
    WHERE id = :id
    LIMIT 1
""")
    suspend fun getById(
        id: Long
    ): PhotoRecord?




    @Query("""
        SELECT *
        FROM photos
        WHERE
        ocrText LIKE '%' || :text || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun search(
        text: String
    ): List<PhotoRecord>


    @Query("""
        SELECT *
        FROM photos
        WHERE
        ocrText LIKE '%' || :text || '%'
        OR
        additionalText LIKE '%' || :text || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun searchIncludingNotes(
        text: String
    ): List<PhotoRecord>


    @Query("""
        SELECT *
        FROM photos
        WHERE
        category = :category
        AND
        ocrText LIKE '%' || :text || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun searchByCategory(
        category: String,
        text: String
    ): List<PhotoRecord>


    @Query("""
        SELECT *
        FROM photos
        WHERE
        category = :category
        AND
        (
            ocrText LIKE '%' || :text || '%'
            OR
            additionalText LIKE '%' || :text || '%'
        )
        ORDER BY createdAt DESC
    """)
    suspend fun searchByCategoryIncludingNotes(
        category: String,
        text: String
    ): List<PhotoRecord>


    @Query("""
        DELETE FROM photos
        WHERE id = :id
    """)
    suspend fun delete(
        id: Long
    )

    @Query("""
    DELETE FROM photo_attachments
""")
    suspend fun deleteAllAttachments()


    @Query("""
    DELETE FROM photos
""")
    suspend fun deleteAllPhotos()


    @Query("""
    UPDATE photos
    SET additionalText = :notes,
        category = :category
    WHERE id = :id
""")
    suspend fun updateNotesAndCategory(
        id: Long,
        notes: String,
        category: String
    )


    @Query("""
        UPDATE photos
        SET category = :category
        WHERE id = :id
    """)
    suspend fun updateCategory(
        id: Long,
        category: String
    )


    @Query("""
        SELECT COUNT(*)
        FROM photos
        WHERE category = :category
    """)
    suspend fun countByCategory(
        category: String
    ): Int


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
    WHERE photoId = :photoId
""")
    suspend fun deleteAttachments(
        photoId: Long
    )


    @Query("""
        DELETE FROM photo_attachments
        WHERE id = :attachmentId
    """)
    suspend fun deleteAttachment(
        attachmentId: Long
    )
}


