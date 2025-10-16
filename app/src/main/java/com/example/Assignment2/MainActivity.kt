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
import com.example.Assignment2.ui.theme.NetworkCallTheme
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
                        if(bookDao.getBooksByTitle(cloudTitle).isEmpty()) {
                            bookDao.insert(Book(title = cloudTitle.trim(), author = "n/a", year = 0, cover = 9278312)) // FG - arbitrary cover for testing
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
                            "cover" to cover
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

            NavHost(navController = navController, startDestination = "FavouritesScreen", builder = {
                composable("OpenLibraryScreen",){
                    OpenLibrarySearchScreen(navController, addFavourite = addFavourite)
                }
                composable("FavouritesScreen",){
                    FavouritesScreen(navController, bookDao, cloudDb)
                }
                composable("ManualEntryScreen",){
                    ManualEntryScreen(navController, addFavourite = addFavourite, bookDao)
                }
            })
        }
    }
}
