package com.example.Assignment2
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.lifecycle.lifecycleScope

@Composable
fun OpenLibrarySearchScreen(navController: NavController, addFavourite: (String) -> Unit, vm: ImageViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                state.loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.results.isEmpty() && state.query.isNotEmpty() -> {
                    Text("No results", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyVerticalGrid(
                        //columns = GridCells.Adaptive(140.dp),
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.results) { bookDoc ->
                            Card(Modifier.fillMaxWidth().wrapContentHeight()) {
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
                                        text = bookDoc.authors?.joinToString(", ") ?: "No author",
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
                                            if (title.isNotEmpty()){
                                                // launch coroutine from view model to keep database out of composable
                                                addFavourite(title)
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ){ Text("Add to favourites") }
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