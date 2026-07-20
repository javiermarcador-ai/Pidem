package es.fotoindex.app.data

import kotlinx.coroutines.flow.Flow

class PhotoRepository(
    private val dao: PhotoDao
) {

    fun getAll(): Flow<List<Photo>> = dao.getAll()

    fun search(text: String): Flow<List<Photo>> =
        dao.search(text)

    suspend fun getById(id: Long): Photo? =
        dao.getById(id)

    suspend fun insert(photo: Photo): Long =
        dao.insert(photo)

    suspend fun update(photo: Photo) =
        dao.update(photo)

    suspend fun delete(photo: Photo) =
        dao.delete(photo)

    suspend fun deleteAll() =
        dao.deleteAll()
}