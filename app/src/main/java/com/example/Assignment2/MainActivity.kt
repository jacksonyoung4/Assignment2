package com.example.Assignment2

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set up databases
        val db = AppDatabase.getDatabase(this)
        val bookDao = db.bookDao()
        val cloudDb = Firebase.firestore


        // Get favourite books from cloud
        cloudDb.collection("favourites").get()
            .addOnSuccessListener { result ->
                lifecycleScope.launch(Dispatchers.IO) {
                    for (document in result) {

                        // Get fields from book
                        val cloudTitle = document.getString("title").toString()
                        val cloudAuthor = document.getString("author").toString()
                        val cloudYear = document.getLong("year")?.toInt() ?: 0
                        val cloudCover = document.getLong("cover")?.toInt() ?: 0
                        val cloudPersonal = document.getString("personal").toString()

                        // If book doesn't belong to local data base add it
                        if(bookDao.getBooksByTitle(cloudTitle).isEmpty()) {
                            bookDao.insert(Book(title = cloudTitle.trim(), author = cloudAuthor, year = cloudYear, cover = cloudCover, personal = cloudPersonal))
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        setContent {
            val navController = rememberNavController()

            // Function for adding favourite book to local and cloud databases
            val toggleFavourite: (String, String, Int, Int) -> Unit = { title, author, year, cover ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if(bookDao.getBooksByTitle(title).isEmpty()) { // Check book isn't already in database
                        bookDao.insert(Book(title = title.trim(), author = author.trim(), year = year, cover = cover)) // Insert book in DAO

                        // Add book to cloud database
                        val book = hashMapOf(
                            "title" to title.trim(),
                            "author" to author.trim(),
                            "year" to year,
                            "cover" to cover,
                            "personal" to ""
                        )
                        cloudDb.collection("favourites")
                            .document(title.trim())
                            .set(book, SetOptions.merge())

                    }
                    // If it already exists, remove book from DAO and Cloud DB
                    else{
                        bookDao.deleteByTitle(title.trim())
                        cloudDb.collection("favourites")
                            .document(title.trim())
                            .delete()
//                        Toast.makeText(this@MainActivity, "REMOVED from Favourites.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Remove favourite book from cloud database
            val removeFavCloud: (String) -> Unit = { title ->
                lifecycleScope.launch(Dispatchers.IO) {
                    cloudDb.collection("favourites").document(title)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }
            }

            // Add file path to personal book picture in cloud database
            val addPersonalCloud: (String, String) -> Unit = { title, personal ->
                lifecycleScope.launch(Dispatchers.IO) {
                    cloudDb.collection("favourites").document(title).update("personal", personal)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                }
            }

            // Start app on favourites screen
            NavHost(navController = navController, startDestination = "FavouritesScreen", builder = {
                composable("OpenLibraryScreen",){
                    OpenLibrarySearchScreen(navController, toggleFavourite = toggleFavourite)
                }
                composable("FavouritesScreen",){
                    FavouritesScreen(navController, bookDao, removeFavCloud = removeFavCloud, addPersonalCloud = addPersonalCloud)
                }
                composable("ManualEntryScreen",){
                    ManualEntryScreen(navController, toggleFavourite = toggleFavourite)
                }
            })
        }
    }
}
