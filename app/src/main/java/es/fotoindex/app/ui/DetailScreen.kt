package es.fotoindex.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import es.fotoindex.app.viewmodel.PhotoViewModel

import androidx.compose.material3.Button
import es.fotoindex.app.ui.SelectedDocument
import androidx.compose.runtime.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.material3.Checkbox
import es.fotoindex.app.data.DetailState

@Composable
fun DetailScreen(onOpenDocument: (Long) -> Unit) {

    val viewModel: PhotoViewModel = viewModel()

    var searchText by remember {
        mutableStateOf(DetailState.searchText)
    }

    var searchInNotes by remember {
        mutableStateOf(DetailState.searchInNotes)
    }


    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var photoToDelete by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(Unit) {

        if (DetailState.searchText.isBlank()) {

            viewModel.loadPhotos()

        } else {

            viewModel.search(
                DetailState.searchText,
                DetailState.searchInNotes
            )

        }

    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        OutlinedTextField(

            value = searchText,
            onValueChange = {
                searchText = it
                DetailState.searchText = it
                viewModel.search(
                    it,
                    searchInNotes
                )

            },

            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp),

            placeholder = {
                Text("Buscar...")
            },

            singleLine = true

        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = searchInNotes,
                onCheckedChange = {

                    searchInNotes = it

                    DetailState.searchInNotes = it

                    viewModel.search(
                        searchText,
                        searchInNotes
                    )

                }
            )

            Text("Buscar también en notas")

        }


        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(viewModel.photos) { photo ->

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),

                    onClick = {

                        SelectedDocument.photo = photo

                        onOpenDocument(photo.id)

                    }

                ) {

                    Row(
                        modifier = Modifier.padding(8.dp)
                    ) {

                        AsyncImage(
                            model = photo.firstPhoto,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = photo.ocrText.take(120),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = photo.additionalText.take(120),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall
                            )

                        }

                        IconButton(

                            onClick = {

                                photoToDelete = photo.id

                                showDeleteDialog = true

                            }

                        ) {

                            Icon(

                                imageVector = Icons.Default.Delete,

                                contentDescription = "Eliminar"

                            )

                        }

                    }

                }

            }

        }
    }

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {

                showDeleteDialog = false

            },

            title = {

                Text("Eliminar documento")

            },

            text = {

                Text("¿Está seguro de que desea eliminar este documento?")

            },

            confirmButton = {

                Button(

                    onClick = {

                        photoToDelete?.let {

                            viewModel.deletePhoto(it)

                        }

                        showDeleteDialog = false

                    }

                ) {

                    Text("Eliminar")

                }

            },

            dismissButton = {

                Button(

                    onClick = {

                        showDeleteDialog = false

                    }

                ) {

                    Text("Cancelar")

                }

            }

        )

    }

}