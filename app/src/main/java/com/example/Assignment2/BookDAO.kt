package com.example.Assignment2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDAO {
    // Insert one or more books
    @Insert
    suspend fun insert(vararg book: Book)

    // Update one or more books
    @Update
    suspend fun update(vararg book: Book)

    // Delete one or more books
    @Delete
    suspend fun delete(vararg book: Book)

    // Get all books
    @Query("SELECT * FROM books")
    fun getAllBooks(): kotlinx.coroutines.flow.Flow<List<Book>> //coroutine to prevent suspending

    // Get books by title
    @Query("SELECT * FROM books WHERE book_title = :bookTitle")
    suspend fun getBooksByTitle(bookTitle: String): List<Book>

    // Get book by ID
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: Long): Book?

    @Query("DELETE FROM books WHERE book_title = :title")
    suspend fun deleteByTitle(title: String): Int
}