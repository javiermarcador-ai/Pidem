package es.fotoindex.app.database

class PhotoRepository(

    private val photoDao: PhotoDao

) {

    suspend fun insert(photo: PhotoRecord) {

        photoDao.insert(photo)

    }

    suspend fun search(text: String): List<PhotoRecord> {

        return photoDao.search(text)

    }

    suspend fun searchIncludingNotes(text: String): List<PhotoRecord> {

        return photoDao.searchIncludingNotes(text)

    }

    suspend fun getAll(): List<PhotoRecord> {

        return photoDao.getAll()

    }

    suspend fun delete(id: Long) {
        photoDao.delete(id)
    }

    suspend fun updateNotes(
        id: Long,
        notes: String
    ) {

        photoDao.updateNotes(
            id,
            notes
        )

    }


    suspend fun insertAttachment(
        attachment: PhotoAttachment
    ) {

        photoDao.insertAttachment(
            attachment
        )

    }

    suspend fun getAttachments(
        photoId: Long
    ): List<PhotoAttachment> {

        return photoDao.getAttachments(
            photoId
        )

    }

    suspend fun deleteAttachment(
        attachmentId: Long
    ) {

        photoDao.deleteAttachment(
            attachmentId
        )

    }


}