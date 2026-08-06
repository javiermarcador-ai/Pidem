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


class PhotoViewModel(application: Application): AndroidViewModel(application) {

    private val repository = PhotoRepository(


        DatabaseProvider
            .getDatabase(application)
            .photoDao()

    )

    val photos: SnapshotStateList<PhotoRecord> = mutableStateListOf()

    fun loadPhotos() {

        viewModelScope.launch {

            photos.clear()

            photos.addAll(
                repository.getAll()
            )


        }

    }

    fun deletePhoto(id: Long) {

        viewModelScope.launch {

            repository.delete(id)

            loadPhotos()

        }

    }

    fun savePhoto(

        firstPhoto: String,

        secondPhoto: String?,

        ocrText: String,

        additionalText: String

    ) {

        viewModelScope.launch {

            val photo = PhotoRecord(

                firstPhoto = firstPhoto,

                secondPhoto = secondPhoto,

                ocrText = ocrText,

                additionalText = additionalText,

                createdAt = System.currentTimeMillis()

            )



            repository.insert(photo)



            loadPhotos()




        }

    }

    fun search(text: String) {

        viewModelScope.launch {

            photos.clear()

            if (text.isBlank()) {

                photos.addAll(
                    repository.getAll()
                )

            } else {

                photos.addAll(
                    repository.search(text)
                )

            }

        }

    }

}