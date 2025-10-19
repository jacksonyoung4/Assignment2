package com.example.Assignment2

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

@Composable
fun ManualEntryScreen(navController: NavController,
                      addFavourite: (String, String, Int, Int) -> Unit) {

    val context = LocalContext.current

    // Remember text field values
    var bookTitle by rememberSaveable { mutableStateOf("") }
    var bookAuthor by rememberSaveable { mutableStateOf("") }
    var bookYear by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(Modifier.height(30.dp))

        // Text field for adding title
        TextField(
            value = bookTitle,
            onValueChange = { bookTitle = it },
            label = { Text("Enter book title") },
            modifier = Modifier.fillMaxWidth()
        )

        // Text field for adding book author
        TextField(
            value = bookAuthor,
            onValueChange = { bookAuthor = it },
            label = { Text("Enter book author") },
            modifier = Modifier.fillMaxWidth()
        )

        // Text field for adding book year
        TextField(
            value = bookYear,
            // Only allow digits to be inputted into text field
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    bookYear = input
                }
            },
            label = { Text("Enter book year") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {

            // Button to add book to favourites
            Button(
                onClick = {
                    val title = bookTitle
                    if (title.isNotEmpty()) {
                        addFavourite(bookTitle, bookAuthor, bookYear.toInt(), 0)
                        Toast.makeText(context, "${bookTitle} added to favourites", Toast.LENGTH_SHORT).show()
                        bookTitle = ""
                        bookAuthor = ""
                        bookYear = ""
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)

            ) { Text("Add to Favourites") }

            // Go back to favourites screen
            Button(
                onClick = {
                    navController.navigate("FavouritesScreen")
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) { Text(text = "Go to Favourites") }
        }
    }
}