package es.fotoindex.app.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    private val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS photo_attachments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    photoId INTEGER NOT NULL,
                    imagePath TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )

        }
    }

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "fotoindex.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()

            INSTANCE = instance

            instance
        }
    }
}