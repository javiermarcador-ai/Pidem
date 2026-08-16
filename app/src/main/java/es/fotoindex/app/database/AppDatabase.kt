package es.fotoindex.app.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PhotoRecord::class,
        PhotoAttachment::class,
        Category::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    abstract fun categoryDao(): CategoryDao

}