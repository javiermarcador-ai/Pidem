package es.fotoindex.app.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: PhotoDatabase? = null

    fun getDatabase(context: Context): PhotoDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                PhotoDatabase::class.java,
                "fotoindex.db"
            ).build()

            INSTANCE = instance

            instance
        }
    }
}
