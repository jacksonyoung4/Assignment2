package com.example.Assignment2

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.net.toUri
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream


@Composable
fun FavouritesScreen(navController: NavController, bookDao: BookDAO, removeFavCloud: (String) -> Unit, addPersonalCloud: (String, String) -> Unit){
    val context = LocalContext.current
    val books by bookDao.getAllBooks().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    // Get configuration
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Check if tablet
    val tablet = configuration.smallestScreenWidthDp >= 600

    var currentSearch by remember { mutableStateOf("") }
    var photoBook by remember { mutableStateOf<Book?>(null) }

    // Check internet connection
    val connectivityManager = getSystemService(LocalContext.current, ConnectivityManager::class.java)
    val currentNetwork = connectivityManager.getActiveNetwork()
    val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
    val internetConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            //Toast.makeText(context, "Photo Taken!", Toast.LENGTH_SHORT).show()
            val book = photoBook
            if (book != null) {
                val filename = "IMG_${book.id}.jpg"
                val quality = 90
                val file = File(context.filesDir, filename)
                if (bitmap != null){
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                }
                book.personal = file.toUri().toString()
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    bookDao.update(book)
                    addPersonalCloud(book.title, book.personal)
                }

                navController.navigate("FavouritesScreen") {
                    popUpTo("FavouritesScreen") { inclusive = true }
                }

                photoBook = null
            }
        }
    )

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
            LazyVerticalGrid(
                // Number of columns based on orientation
                columns = if (isPortrait || tablet)
                    GridCells.Fixed(1)
                else GridCells.Fixed(2),
                // Grid layout
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(books) { book ->
                    if (book.title.contains(
                            currentSearch,
                            ignoreCase = true
                        ) || currentSearch.isEmpty()
                    ) {
                        Card(Modifier.fillMaxWidth().wrapContentHeight()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {

                                val imageModel =
                                    if (book.personal.isNotEmpty() &&
                                        Uri.parse(book.personal).path?.let { File(it).exists() && File(it).length() > 0 } == true)
                                        book.personal
                                    else
                                        "https://covers.openlibrary.org/b/id/${book.cover}-M.jpg"


                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = book.cover.toString(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .wrapContentHeight()
                                )
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "${book.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(text = "${book.author}")
                                    Text(text = "${book.year}")
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                bookDao.delete(book)  // deletes book from DAO
                                                removeFavCloud(book.title) // deletes book from cloud
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text("Unfavourite")
                                    }
                                    Button(
                                        onClick = {
                                            photoBook = book
                                            takePhoto.launch(null)
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text("Take Cover Photo")
                                    }
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
