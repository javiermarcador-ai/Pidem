package es.fotoindex.app.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

}