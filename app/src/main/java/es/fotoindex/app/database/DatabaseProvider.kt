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


    private val MIGRATION_2_3 = object : Migration(2, 3) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {

            database.execSQL(
                """
                ALTER TABLE photos
                ADD COLUMN category TEXT NOT NULL DEFAULT 'Documentos'
                """.trimIndent()
            )
        }
    }


    private val MIGRATION_3_4 = object : Migration(3, 4) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                INSERT INTO categories (name)
                SELECT 'Documentos'
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM categories
                    WHERE name = 'Documentos'
                )
                """.trimIndent()
            )
        }
    }


    @Volatile
    private var INSTANCE: AppDatabase? = null


    fun getDatabase(
        context: Context
    ): AppDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "fotoindex.db"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .build()

            INSTANCE = instance

            instance
        }
    }
}

