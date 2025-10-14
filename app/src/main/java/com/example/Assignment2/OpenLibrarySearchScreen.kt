package com.example.Assignment2
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun OpenLibrarySearchScreen(navController: NavController, addFavourite: (String) -> Unit, vm: ImageViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    // Get configuration
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Check if tablet
    val tablet = configuration.smallestScreenWidthDp >= 600

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // Search box for open library
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search Open Library") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { vm.search() })
        )

        Spacer(Modifier.height(12.dp))

        // Box for results
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {

                // Loading indicator
                state.loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                // Error message
                state.error != null -> {
                    Text(
                        text = state.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // No results message
                state.results.isEmpty() && state.query.isNotEmpty() -> {
                    Text("No results", modifier = Modifier.align(Alignment.Center))
                }


                else -> {

                    LazyVerticalGrid(
                        // Number of columns based on orientation
                        columns = if (isPortrait || tablet)
                            GridCells.Fixed(1)
                        else GridCells.Fixed(2),
                        // Grid layout
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {

                        // For each book in the search results
                        items(state.results) { bookDoc ->

                            // Card for each book
                            Card(Modifier.fillMaxWidth().wrapContentHeight()) {
                                // Phone portrait or landscape
                                if (!tablet) {
                                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                        AsyncImage(
                                            model = bookDoc.coverUrl,
                                            contentDescription = bookDoc.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().height(180.dp)
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = bookDoc.title ?: "",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = bookDoc.authors?.joinToString(", ")
                                                ?: "No author",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "${bookDoc.firstPublishYear}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        // Fav Button to add to database
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                val title = bookDoc.title.orEmpty().trim()
                                                if (title.isNotEmpty()) {
                                                    // launch coroutine from view model to keep database out of composable
                                                    addFavourite(title)
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) { Text("Add to favourites") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = {
                    navController.navigate("FavouritesScreen")
                }) {
                Text(text = "Go to Favourites")
            }
        }
    }
}