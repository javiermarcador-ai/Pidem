package es.fotoindex.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import es.fotoindex.app.database.DatabaseProvider
import es.fotoindex.app.database.PhotoRecord
import es.fotoindex.app.database.PhotoRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import es.fotoindex.app.database.Category
import es.fotoindex.app.ui.CameraPreview
import es.fotoindex.app.data.PidemStorage
import es.fotoindex.app.data.PidemExporter
import es.fotoindex.app.data.PidemImporter

class PhotoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        DatabaseProvider.getDatabase(application)

    private val repository =
        PhotoRepository(
            photoDao = database.photoDao(),
            categoryDao = database.categoryDao()
        )

    val photos: SnapshotStateList<PhotoRecord> =
        mutableStateListOf()



    /*
     * CATEGORÍAS
     */

    val categories: SnapshotStateList<Category> =
        mutableStateListOf()

    var selectedCategory by mutableStateOf("Documentos")

    /*
     * Cargar fotografías
     */

    fun loadPhotos() {

        viewModelScope.launch {

            photos.clear()

            if (selectedCategory == "Todas") {

                photos.addAll(
                    repository.getAll()
                )

            } else {

                photos.addAll(
                    repository.getByCategory(
                        selectedCategory
                    )
                )

            }

        }

    }


    /*
     * Cargar categorías
     *
     * De momento mantenemos las categorías
     * definidas en el propio documento.
     *
     * La gestión persistente de categorías
     * la añadiremos en el siguiente paso.
     */



    /*
     * Seleccionar categoría
     */

    fun selectCategory(category: String) {

        /*
         * "Todas" es solamente un filtro.
         * No es una categoría que pueda asignarse
         * a una fotografía.
         */
        if (
            category != "Todas" &&
            categories.none {
                it.name == category
            }
        ) {
            return
        }

        selectedCategory = category

        loadPhotos()
    }



    /*     * Eliminar fotografía     */

    /*
     * Eliminar una fotografía/documento
     *
     * Se eliminan:
     * - fotografía principal
     * - segunda fotografía, si existe
     * - fotografías adjuntas
     * - registros de adjuntos
     * - registro principal de Room
     */
    fun deletePhoto(
        id: Long
    ) {
        viewModelScope.launch {
            val photo =                repository.getById(id)
            if (photo != null) {
                /*
                 * Fotografías principales
                 */
                PidemStorage.deleteImage(
                    getApplication(),
                    photo.firstPhoto
                )

                photo.secondPhoto?.let { path ->

                    PidemStorage.deleteImage(
                        getApplication(),
                        path
                    )
                }

                /*
                 * Fotografías adjuntas
                 */
                val attachments =
                    repository.getAttachments(id)

                attachments.forEach { attachment ->

                    PidemStorage.deleteImage(
                        getApplication(),
                        attachment.imagePath
                    )

                    repository.deleteAttachment(
                        attachment.id
                    )
                }

                /*
                 * Finalmente eliminamos
                 * el registro principal.
                 */
                repository.delete(id)
            }

            loadPhotos()
        }
    }

    fun deleteAllData(
        onFinished: (Boolean) -> Unit = {}
    ) {

        viewModelScope.launch {
            repository.deleteAll()
            repository.deleteAllCategories()
            repository.insertCategory(
                Category( name = "Documentos"  )
            )

            val storageDeleted =
                es.fotoindex.app.data.PidemStorage
                    .deletePidemFolder(
                        getApplication()
                    )

            photos.clear()
            categories.clear()
            categories.add(
                Category( name = "Documentos" )
            )

            selectedCategory = "Documentos"
            onFinished(
                storageDeleted
            )
        }
    }


    /*
     * Eliminar varias fotografías/documentos
     */
    fun deletePhotos(
        ids: Set<Long>
    ) {

        viewModelScope.launch {

            ids.forEach { id ->

                val photo =
                    repository.getById(id)

                if (photo != null) {

                    /*
                     * Fotografías principales
                     */
                    PidemStorage.deleteImage(
                        getApplication(),
                        photo.firstPhoto
                    )

                    photo.secondPhoto?.let { path ->

                        PidemStorage.deleteImage(
                            getApplication(),
                            path
                        )
                    }

                    /*
                     * Fotografías adjuntas
                     */
                    val attachments =
                        repository.getAttachments(id)

                    attachments.forEach { attachment ->

                        PidemStorage.deleteImage(
                            getApplication(),
                            attachment.imagePath
                        )

                        repository.deleteAttachment(
                            attachment.id
                        )
                    }

                    /*
                     * Registro principal
                     */
                    repository.delete(id)
                }
            }

            loadPhotos()
        }
    }



    fun updateNotesAndCategory(
        id: Long,
        notes: String,
        category: String
    ) {

        viewModelScope.launch {

            repository.updateNotesAndCategory(
                id = id,
                notes = notes,
                category = category
            )

            loadPhotos()
        }
    }
    suspend fun loadCategories() {
        categories.clear()
        val loadedCategories =   repository.getCategories()
        if (
            loadedCategories.none {
                it.name.equals(
                    "Documentos",
                    ignoreCase = true
                )
            }
        ) {
            repository.insertCategory(
                Category(  name = "Documentos"    )
            )
        }

        categories.addAll( repository.getCategories()  )

        if (
            selectedCategory != "Todas" &&
            categories.none {
                it.name.equals(
                    selectedCategory,
                    ignoreCase = true
                )
            }
        ) {
            selectedCategory = "Documentos"
        }
    }


    suspend fun loadCategoriesAndWait() {

        categories.clear()

        val loadedCategories =
            repository.getCategories()

        if (
            loadedCategories.none {
                it.name.equals(
                    "Documentos",
                    ignoreCase = true
                )
            }
        ) {

            repository.insertCategory(
                Category(
                    name = "Documentos"
                )
            )
        }

        categories.addAll(
            repository.getCategories()
        )

        if (
            selectedCategory != "Todas" &&
            categories.none {
                it.name.equals(
                    selectedCategory,
                    ignoreCase = true
                )
            }
        ) {

            selectedCategory = "Documentos"
        }
    }


    /*
     * Guardar fotografía
     */
    fun savePhoto(
        firstPhoto: String,
        secondPhoto: String?,
        ocrText: String,
        additionalText: String,
        category: String,
        onSaved: () -> Unit = {}
    ) {

        viewModelScope.launch {

            val finalCategory =
                if (
                    category == "Todas" ||
                    category.isBlank()
                ) {
                    "Documentos"
                } else {
                    category
                }

            val photo = PhotoRecord(
                firstPhoto = firstPhoto,
                secondPhoto = secondPhoto,
                ocrText = ocrText,
                additionalText = additionalText,
                category = finalCategory,
                createdAt = System.currentTimeMillis()
            )

            repository.insert(photo)
            loadPhotos()
            onSaved()
        }
    }


    fun exportData(
        onFinished: (Result<String>) -> Unit
    ) {

        viewModelScope.launch {
            try {
                val photos =  repository.getAll()
                val attachments =     repository.getAllAttachments()
                val categories =      repository.getCategories()
                val result =
                    PidemExporter.export(
                        context = getApplication(),
                        photos = photos,
                        attachments = attachments,
                        categories = categories
                    )
                onFinished(result)

            } catch (e: Exception) {
                onFinished(
                    Result.failure(e)
                )
            }
        }
    }

    fun importData(
        fileUri: android.net.Uri,
        replaceExisting: Boolean,
        onFinished: (Result<String>) -> Unit
    ) {

        viewModelScope.launch {

            try {

                /*
                 * =====================================================
                 * LEER DATOS DEL ARCHIVO
                 * =====================================================
                 */

                val importedResult =
                    PidemImporter.import(
                        context = getApplication(),
                        fileUri = fileUri
                    )

                if (importedResult.isFailure) {

                    onFinished(
                        Result.failure(
                            importedResult.exceptionOrNull()
                                ?: Exception(
                                    "No se pudieron leer los datos del archivo"
                                )
                        )
                    )

                    return@launch
                }


                val importedData =
                    importedResult.getOrThrow()


                /*
                 * =====================================================
                 * BORRAR DATOS ACTUALES
                 * =====================================================
                 */

                if (replaceExisting) {

                    repository.deleteAll()

                    PidemStorage.deletePidemFolder(
                        getApplication()
                    )
                }


                /*
                 * Documentos debe existir siempre.
                 */

                if (
                    repository.getCategoryByName(
                        "Documentos"
                    ) == null
                ) {

                    repository.insertCategory(
                        Category(
                            name = "Documentos"
                        )
                    )
                }


                /*
                 * =====================================================
                 * EXTRAER IMÁGENES
                 * =====================================================
                 */

                val extractedResult =
                    PidemImporter.extractAllImages(
                        context = getApplication(),
                        fileUri = fileUri,
                        data = importedData
                    )


                if (extractedResult.isFailure) {

                    onFinished(
                        Result.failure(
                            extractedResult.exceptionOrNull()
                                ?: Exception(
                                    "No se pudieron extraer las imágenes"
                                )
                        )
                    )

                    return@launch
                }


                val extracted =
                    extractedResult.getOrThrow()


                val firstPhotos =
                    extracted.first

                val secondPhotos =
                    extracted.second

                val attachmentImages =
                    extracted.third


                /*
                 * =====================================================
                 * FOTOGRAFÍAS
                 * =====================================================
                 */

                val photoIdMap =
                    mutableMapOf<Long, Long>()


                importedData.photos.forEach { importedPhoto ->

                    val firstPhotoPath =
                        firstPhotos[
                            importedPhoto.oldId
                        ]


                    if (firstPhotoPath == null) {

                        throw Exception(
                            "No se encontró la imagen principal de la fotografía " +
                                    importedPhoto.oldId
                        )
                    }


                    val secondPhotoPath =
                        secondPhotos[
                            importedPhoto.oldId
                        ]


                    /*
                     * Comprobar que la categoría existe.
                     * Si no existe, utilizamos Documentos.
                     */

                    val categoryName =
                        if (
                            repository.getCategoryByName(
                                importedPhoto.category
                            ) != null
                        ) {
                            importedPhoto.category
                        } else {
                            "Documentos"
                        }


                    val photo =
                        PhotoRecord(

                            firstPhoto =
                                firstPhotoPath,

                            secondPhoto =
                                secondPhotoPath,

                            ocrText =
                                importedPhoto.ocrText,

                            additionalText =
                                importedPhoto.additionalText,

                            category =
                                categoryName,

                            createdAt =
                                importedPhoto.createdAt
                        )


                    /*
                     * Room devuelve directamente el nuevo ID.
                     */

                    val newId =
                        repository.insert(
                            photo
                        )


                    photoIdMap[
                        importedPhoto.oldId
                    ] =
                        newId
                }


                /*
                 * =====================================================
                 * ADJUNTOS
                 * =====================================================
                 */

                importedData.attachments.forEach { importedAttachment ->

                    val newPhotoId =
                        photoIdMap[
                            importedAttachment.oldPhotoId
                        ]
                            ?: throw Exception(
                                "No se encontró la fotografía asociada " +
                                        "al adjunto ${importedAttachment.oldId}"
                            )


                    val imagePath =
                        attachmentImages[
                            importedAttachment.oldId
                        ]
                            ?: throw Exception(
                                "No se encontró la imagen del adjunto " +
                                        importedAttachment.oldId
                            )


                    repository.insertAttachment(

                        es.fotoindex.app.database.PhotoAttachment(

                            photoId =
                                newPhotoId,

                            imagePath =
                                imagePath,

                            createdAt =
                                importedAttachment.createdAt
                        )
                    )
                }


                /*
                 * =====================================================
                 * ACTUALIZAR LISTADOS
                 * =====================================================
                 */

                loadCategories()

                loadPhotos()


                onFinished(
                    Result.success(
                        "Importación completada correctamente.\n\n" +
                                "Fotografías importadas: " +
                                importedData.photos.size +
                                "\nAdjuntos importados: " +
                                importedData.attachments.size +
                                "\n\nLos documentos importados sin categoría existente " +
                                "\nse incluirán en la categoría \"Documentos\"."
                    )
                )


            } catch (e: Exception) {

                onFinished(
                    Result.failure(e)
                )
            }
        }
    }


    /*
     * Búsqueda
     */
    fun search(
        text: String,
        searchInNotes: Boolean
    ) {

        viewModelScope.launch {

            photos.clear()

            if (text.isBlank()) {

                if (selectedCategory == "Todas") {

                    photos.addAll(
                        repository.getAll()
                    )

                } else {

                    photos.addAll(
                        repository.getByCategory(
                            selectedCategory
                        )
                    )

                }

            } else {

                if (selectedCategory == "Todas") {

                    if (searchInNotes) {
                        photos.addAll(repository.searchIncludingNotes(text))
                    } else {
                        photos.addAll(repository.search(text))
                    }

                } else {

                    if (searchInNotes) {

                        photos.addAll(
                            repository.searchByCategoryIncludingNotes(
                                selectedCategory,
                                text
                            )
                        )

                    } else {

                        photos.addAll(
                            repository.searchByCategory(
                                selectedCategory,
                                text
                            )
                        )

                    }

                }

            }

        }

    }


    /*
     * ADJUNTOS
     */

    fun loadAttachments(
        photoId: Long,
        target: SnapshotStateList<es.fotoindex.app.database.PhotoAttachment>
    ) {

        viewModelScope.launch {

            target.clear()

            target.addAll(
                repository.getAttachments(
                    photoId
                )
            )

        }

    }


    fun addAttachment(
        photoId: Long,
        imagePath: String
    ) {

        viewModelScope.launch {

            repository.insertAttachment(

                es.fotoindex.app.database.PhotoAttachment(

                    photoId = photoId,

                    imagePath = imagePath

                )

            )

        }

    }


    fun deleteAttachment(
        attachmentId: Long
    ) {

        viewModelScope.launch {

            repository.deleteAttachment(
                attachmentId
            )

        }

    }


    fun insertCategory(name: String) {

        viewModelScope.launch {

            repository.insertCategory(
                Category(
                    name = name
                )
            )

            loadCategories()
        }
    }



    fun updateCategory(
        id: Long,
        newName: String
    ) {

        viewModelScope.launch {

            val currentCategory =
                repository.getCategoryByName(
                    "Documentos"
                )

            /*
             * Documentos no se puede modificar.
             */
            if (
                currentCategory != null &&
                currentCategory.id == id
            ) {
                return@launch
            }

            val cleanName =
                newName.trim()

            /*
             * No permitimos nombres vacíos
             * ni nombres reservados.
             */
            if (
                cleanName.isBlank() ||
                cleanName.equals(
                    "Todas",
                    ignoreCase = true
                ) ||
                cleanName == "+"
            ) {
                return@launch
            }

            /*
             * Evitamos duplicar categorías.
             */
            val existing =
                repository.getCategoryByName(
                    cleanName
                )

            if (
                existing != null &&
                existing.id != id
            ) {
                return@launch
            }

            repository.updateCategory(
                Category(
                    id = id,
                    name = cleanName
                )
            )

            loadCategories()
        }
    }


    fun deleteCategory(id: Long) {

        viewModelScope.launch {

            val category =
                repository.getCategories()
                    .find { it.id == id }

            /*
             * Documentos nunca se puede borrar.
             */
            if (
                category == null ||
                category.name.equals(
                    "Documentos",
                    ignoreCase = true
                )
            ) {
                return@launch
            }

            /*
             * IMPORTANTE:
             *
             * NO modificamos las fotografías que
             * pertenecían a esta categoría.
             *
             * Sus registros conservarán el nombre
             * antiguo en Room.
             *
             * DocumentScreen se encargará de
             * considerar "Documentos" una categoría
             * que ya no exista.
             */
            repository.deleteCategory(id)

            loadCategories()

            /*
             * Si la categoría que estaba seleccionada
             * era precisamente la eliminada, volvemos
             * a Documentos.
             */
            if (selectedCategory == category.name) {
                selectedCategory = "Documentos"
                loadPhotos()
            }
        }
    }

    fun getValidCategory(
        category: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {

            val exists =
                repository.getCategoryByName(category) != null

            if (exists) {
                onResult(category)
            } else {
                onResult("Documentos")
            }
        }
    }


    fun normalizePhotoCategory(
        id: Long,
        category: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {

            val exists =
                repository.getCategoryByName(category) != null

            if (exists) {

                onResult(category)

            } else {

                repository.updatePhotoCategory(
                    id = id,
                    category = "Documentos"
                )

                onResult("Documentos")
            }
        }
    }


}

