package com.example.Assignment2

import android.content.ContentValues.TAG
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage


@Composable
fun FavouritesScreen(navController: NavController, bookDao: BookDAO, cloudDb: FirebaseFirestore){
    val books by bookDao.getAllBooks().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var currentSearch by remember { mutableStateOf("") }

    // Check internet connection
    val connectivityManager = getSystemService(LocalContext.current, ConnectivityManager::class.java)
    val currentNetwork = connectivityManager.getActiveNetwork()
    val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
    val internetConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

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

        Spacer(Modifier.height(16.dp))
        // Search box for open library
        OutlinedTextField(
            value = currentSearch,
            onValueChange = { currentSearch = it },
            label = { Text("Search Favourites") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (books.isEmpty()) {
            Text("No favourites yet.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f), // FG - changed from maxSize as was blocking button, takes remaining space
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    if (book.title.contains(currentSearch, ignoreCase = true) || currentSearch.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            AsyncImage(model = "https://covers.openlibrary.org/b/id/${book.cover}-M.jpg",
                                contentDescription = book.cover.toString(),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(180.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    //modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(text = "${book.id} - ${book.title}")
                                    Text(text = "${book.author}")
                                    Text(text = "${book.year}")
                                }
                                Button(
                                    onClick = {
                                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            bookDao.delete(book)  // deletes book from DAO
                                            // TODO - need to delete from cloud
                                        }
                                    }
                                ) {
                                    Text("UnFav")
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
            if (internetConnected) {
                navController.navigate("OpenLibraryScreen")
            }
            else{
                navController.navigate("ManualEntryScreen")
            }
        }) {
            Text(text = "Add Books")
        }
    }
}
