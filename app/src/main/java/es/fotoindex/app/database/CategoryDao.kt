package es.fotoindex.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(
        category: Category
    ): Long

    @Query("""
        SELECT *
        FROM categories
        ORDER BY name COLLATE NOCASE ASC
    """)
    suspend fun getAll(): List<Category>

    @Query("""
        SELECT *
        FROM categories
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getById(
        id: Long
    ): Category?

    @Query("""
        SELECT *
        FROM categories
        WHERE name = :name
        LIMIT 1
    """)
    suspend fun getByName(
        name: String
    ): Category?

    @Update
    suspend fun update(
        category: Category
    )

    @Query("""
        DELETE FROM categories
        WHERE id = :id
    """)
    suspend fun delete(
        id: Long
    )

    @Query("""
        SELECT COUNT(*)
        FROM categories
    """)
    suspend fun count(): Int
}