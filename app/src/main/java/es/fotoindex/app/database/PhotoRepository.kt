package es.fotoindex.app.database

class PhotoRepository(

    private val photoDao: PhotoDao,
    private val categoryDao: CategoryDao

) {

    suspend fun insert(
        photo: PhotoRecord
    ) {

        photoDao.insert(photo)

    }


    suspend fun getAll(): List<PhotoRecord> {

        return photoDao.getAll()

    }


    suspend fun getByCategory(
        category: String
    ): List<PhotoRecord> {

        return photoDao.getByCategory(
            category
        )

    }

    suspend fun getById(
        id: Long
    ): PhotoRecord? {

        return photoDao.getById(
            id
        )

    }





    suspend fun search(
        text: String
    ): List<PhotoRecord> {

        return photoDao.search(text)

    }


    suspend fun searchIncludingNotes(
        text: String
    ): List<PhotoRecord> {

        return photoDao.searchIncludingNotes(
            text
        )

    }


    suspend fun searchByCategory(
        category: String,
        text: String
    ): List<PhotoRecord> {

        return photoDao.searchByCategory(
            category,
            text
        )

    }


    suspend fun searchByCategoryIncludingNotes(
        category: String,
        text: String
    ): List<PhotoRecord> {

        return photoDao.searchByCategoryIncludingNotes(
            category,
            text
        )

    }


    suspend fun delete(      id: Long    ) {
        photoDao.delete(id)
    }

    suspend fun deleteAll() {
        photoDao.deleteAllAttachments()
        photoDao.deleteAllPhotos()
    }


    suspend fun updateNotesAndCategory(
        id: Long,
        notes: String,
        category: String
    ) {

        photoDao.updateNotesAndCategory(
            id = id,
            notes = notes,
            category = category
        )

    }

    suspend fun countByCategory(
        category: String
    ): Int {

        return photoDao.countByCategory(
            category
        )

    }


    /*
     * CATEGORÍAS
     */

    suspend fun getCategories(): List<Category> {

        return categoryDao.getAll()

    }


    suspend fun getCategoryByName(
        name: String
    ): Category? {

        return categoryDao.getByName(name)

    }


    suspend fun insertCategory(
        category: Category
    ): Long {

        return categoryDao.insert(
            category
        )

    }


    suspend fun updateCategory(
        category: Category
    ) {

        categoryDao.update(
            category
        )

    }


    suspend fun deleteCategory(
        id: Long
    ) {

        categoryDao.delete(
            id
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

    suspend fun deleteAttachments(
        photoId: Long
    ) {

        photoDao.deleteAttachments(
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


    suspend fun updatePhotoCategory(
        id: Long,
        category: String
    ) {
        photoDao.updateCategory(
            id,
            category
        )
    }



}

