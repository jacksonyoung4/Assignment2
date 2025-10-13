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

        setContent {
            val navController = rememberNavController()

            val addFavourite: (String) -> Unit = { title ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if(bookDao.getBooksByTitle(title).isEmpty()) { //no duplicates
                        bookDao.insert(Book(title = title.trim()))
                        val book = hashMapOf(
                            "title" to title.trim()
                        )
                        cloudDb.collection("favourites")
                            .document(title.trim())
                            .set(book, SetOptions.merge())
                    }
                }
            }

            NavHost(navController = navController, startDestination = "FavouritesScreen", builder = {
                composable("OpenLibraryScreen",){
                    OpenLibrarySearchScreen(navController, addFavourite = addFavourite)
                }
                composable("FavouritesScreen",){
                    FavouritesScreen(navController, bookDao )
                }
            })
        }
    }
}
