package es.fotoindex.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Photo::class],
    version = 1,
    exportSchema = false
)
abstract class PhotoDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

}