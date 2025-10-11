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

@Composable
fun OpenLibrarySearchScreen(vm: ImageViewModel = viewModel()) {
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

        Box(Modifier.fillMaxSize()) {
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
                                    Text(
                                        text = bookDoc.title ?: ""
                                    )
                                    Text(

                                        text = bookDoc.authors?.joinToString(", ") ?: "No author"
                                    )
                                    Text(
                                        text = "${bookDoc.firstPublishYear}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}