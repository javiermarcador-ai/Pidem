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
    suspend fun getAll(): List<PhotoRecord> {

        return photoDao.getAll()

    }

    suspend fun delete(id: Long) {
        photoDao.delete(id)
    }


}