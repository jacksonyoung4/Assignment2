package com.example.Assignment2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

@Composable
fun ManualEntryScreen(navController: NavController, addFavourite: (String, String, Int, Int) -> Unit, bookDao: BookDAO) {

    // Search box for open library
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var bookYear by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = bookTitle,
            onValueChange = { bookTitle = it },
            label = { Text("Enter book title") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = bookAuthor,
            onValueChange = { bookAuthor = it },
            label = { Text("Enter book author") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = bookYear,
            onValueChange = { bookYear = it },
            label = { Text("Enter book year") }, //currently no checks to ensure int
            modifier = Modifier.fillMaxWidth()
        )

        // Fav Button to add to database
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                val title = bookTitle
                if (title.isNotEmpty()) {
                    addFavourite(bookTitle, bookAuthor, bookYear.toInt(), 9278312)  // FG - arbitrary cover
                }                                                                   // (can make if equal to certain number -> don't show image)
            },
        ) { Text("Add to favourites") }

        // Go back to favourites screen
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                navController.navigate("FavouritesScreen")
            }
        ) { Text(text = "Go to Favourites") }
    }
}