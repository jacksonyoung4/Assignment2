package com.example.Assignment2

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.net.toUri
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import android.Manifest

@Composable
fun FavouritesScreen(navController: NavController, bookDao: BookDAO,
                     removeFavCloud: (String) -> Unit, addPersonalCloud: (String, String) -> Unit){

    val context = LocalContext.current
    val books by bookDao.getAllBooks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Get configuration
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Check if tablet
    val tablet = configuration.smallestScreenWidthDp >= 600

    // Remember current search and book for if photo it to be taken
    var currentSearch by rememberSaveable { mutableStateOf("") }
    var photoBook by remember { mutableStateOf<Book?>(null) }

    // Remember current book and contact number for share
    var shareBook by remember { mutableStateOf<Book?>(null) }
    var pickedNum by remember {mutableStateOf<String?>(null)}

    // Manager for checking internet connection
    val connectivityManager = getSystemService(LocalContext.current, ConnectivityManager::class.java)

    // Set up activity to launch expecting a result
    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), // Activity for taking picture
        onResult = { bitmap ->

            val book = photoBook // Book taking photo for

            if (book != null) {
                val filename = "IMG_${book.id}.jpg" // Use book ID for file name
                val quality = 90
                val file = File(context.filesDir, filename) // Create file

                // Write image to file
                if (bitmap != null){
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                }

                // Update book personal image field with URI (essentially file path)
                book.personal = file.toUri().toString()

                // Update the book with personal image location
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    bookDao.update(book)
                    addPersonalCloud(book.title, book.personal)
                }

                // Reload favourites screen so image populates
                navController.navigate("FavouritesScreen") {
                    popUpTo("FavouritesScreen") { inclusive = true }
                }

                photoBook = null
            }
        }
    )

    // pick contact activity for share function
    val pickContact = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            // Query contact to get phone number - from workshop 7
            val contactsProjection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
            )
            context.contentResolver.query(contactUri, contactsProjection, null, null, null)
                ?.use { c ->
                    if (c.moveToFirst()) {
                        val contactId = c.getString(
                            c.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                        )
                        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
                            arrayOf(contactId),
                            null
                        )?.use { pc ->
                            pickedNum = if (pc.moveToFirst()) {
                                pc.getString(
                                    pc.getColumnIndexOrThrow(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    )
                                )
                            } else null
                        }
                    }
                }

            // SMS intent if number and book are valid
            if (pickedNum != null && shareBook != null) {
                val smsUri = "smsto:$pickedNum".toUri()
                val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                    putExtra(
                        "sms_body",
                        "Hey! Check out this cool book: \n\n${shareBook?.title} \n${shareBook?.author}\n ${shareBook?.year.toString()}"
                    )
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Contact error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // contact request
    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContact.launch(null)
        }
        else Toast.makeText(
            context, "Permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Favourites heading at top of screen
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

        // Display message if no favourite books
        if (books.isEmpty()) {
            Text("No favourites yet.", style = MaterialTheme.typography.bodyLarge)

        } else {
            LazyVerticalGrid(

                // Number of columns based on device orientation
                columns = if (isPortrait && !tablet)
                    GridCells.Fixed(1) // 1 column for portrait
                else GridCells.Fixed(2), // 2 columns for landscape or tablet

                // Grid layout
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(books) { book ->

                    // Only display book its title or author matches search term (or no search)
                    if (book.title.startsWith(currentSearch, ignoreCase = true)
                        || (book.author.startsWith(currentSearch, ignoreCase = true))
                        || currentSearch.isEmpty()
                    ) {

                        Card(Modifier.fillMaxWidth().wrapContentHeight()) {

                            // Row for image on left, book info on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {

                                val image =
                                    if (book.personal.isNotEmpty() && // Check if personal image URI exists
                                        // Convert string to URI and get image path
                                        Uri.parse(book.personal).path?.let {
                                            // Check if file at path exists and is not empty
                                            File(it).exists() && File(it).length() > 0 } == true)
                                        // If conditions are met set to display personal image
                                        book.personal

                                    // Use cover image if no personal image
                                    else
                                        "https://covers.openlibrary.org/b/id/${book.cover}-M.jpg"

                                // Display cover/personal image
                                AsyncImage(
                                    model = image,
                                    contentDescription = book.cover.toString(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .wrapContentHeight()
                                )

                                // Column for book info
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

                                    // Delete button
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

                                    // Button for taking book cover picture
                                    Button(
                                        onClick = {
                                            photoBook = book // Assign book for taking picture
                                            takePhoto.launch(null) // Launch activity for taking pic expecting result
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text("Take Cover Pic")
                                    }

                                    // Button to Share Book
                                    Button(
                                        onClick = {
                                            shareBook = book // Assign book for sharing
                                            requestPermission.launch(Manifest.permission.READ_CONTACTS)// Launch activity for sharing expecting result
                                        },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text("Share")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Button to navigate to screen for adding books
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {

                // Check if internet is connected
                val currentNetwork = connectivityManager.getActiveNetwork()
                val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
                val internetConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                // If internet is connected, navigate to screen for open library search
                if (internetConnected) {
                    navController.navigate("OpenLibraryScreen")
                }
                // If no internet, navigate to manual entry screen
                else{
                    navController.navigate("ManualEntryScreen")
                }
            }) {
            Text(text = "Add Books")
        }
    }
}
