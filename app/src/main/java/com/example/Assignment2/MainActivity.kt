package com.example.Assignment2

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

        val db = AppDatabase.getDatabase(this)
        val cloudDb = Firebase.firestore
        val bookDao = db.bookDao()

        cloudDb.collection("favourites").get()
            .addOnSuccessListener { result ->
                lifecycleScope.launch(Dispatchers.IO) {
                    for (document in result) {
                        val cloudTitle = document.getString("title").toString()
                        val cloudAuthor = document.getString("author").toString()
                        val cloudYear = document.getLong("year")?.toInt() ?: 0
                        val cloudCover = document.getLong("cover")?.toInt() ?: 0
                        val cloudPersonal = document.getString("personal").toString()
                        if(bookDao.getBooksByTitle(cloudTitle).isEmpty()) {
                            bookDao.insert(Book(title = cloudTitle.trim(), author = cloudAuthor, year = cloudYear, cover = cloudCover, personal = cloudPersonal)) // FG - arbitrary cover for testing
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        setContent {
            val navController = rememberNavController()

            val addFavourite: (String, String, Int, Int) -> Unit = { title, author, year, cover ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if(bookDao.getBooksByTitle(title).isEmpty()) { //no duplicates
                        bookDao.insert(Book(title = title.trim(), author = author.trim(), year = year, cover = cover))
                        val book = hashMapOf(
                            "title" to title.trim(),
                            "author" to author.trim(),
                            "year" to year,
                            "cover" to cover,
                            "personal" to ""
                        )
                        cloudDb.collection("favourites") // TODO - not properly saving in database, add columns? didnt pull extra info from restart
                            .document(title.trim())
                            .set(book, SetOptions.merge())
                    }
                    else{
                        bookDao.deleteByTitle(title.trim())
                        cloudDb.collection("favourites")
                            .document(title.trim())
                            .delete()
                    }
                }
            }

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

            NavHost(navController = navController, startDestination = "FavouritesScreen", builder = {
                composable("OpenLibraryScreen",){
                    OpenLibrarySearchScreen(navController, addFavourite = addFavourite)
                }
                composable("FavouritesScreen",){
                    FavouritesScreen(navController, bookDao, removeFavCloud = removeFavCloud, addPersonalCloud = addPersonalCloud)
                }
                composable("ManualEntryScreen",){
                    ManualEntryScreen(navController, addFavourite = addFavourite, bookDao)
                }
            })
        }
    }
}
