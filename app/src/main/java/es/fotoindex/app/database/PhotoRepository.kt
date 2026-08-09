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
}