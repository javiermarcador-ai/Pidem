package es.fotoindex.app.database

class PhotoRepository(

    private val photoDao: PhotoDao

) {

    suspend fun insert(photo: PhotoRecord) {

        photoDao.insert(photo)

    }

    suspend fun getAll(): List<PhotoRecord> {

        return photoDao.getAll()

    }

}