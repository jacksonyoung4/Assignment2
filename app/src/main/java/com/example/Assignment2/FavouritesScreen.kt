package com.example.Assignment2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch


@Composable
fun FavouritesScreen(navController: NavController, bookDao: BookDAO){
    val books by bookDao.getAllBooks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Favourites",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Button(onClick = {
            navController.navigate("OpenLibraryScreen")
        }) {
            Text(text = "Go to book search")
        }

        Spacer(Modifier.height(16.dp))

        if (books.isEmpty()) {
            Text("No favourites yet.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "${book.id} - ${book.title}")
                        Button(
                            onClick = {
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    bookDao.delete(book)  // deletes book from DAO
                                }
                            }
                        ){
                            Text("UnFav")
                        }
                    }
                }
            }
        }
    }
}
