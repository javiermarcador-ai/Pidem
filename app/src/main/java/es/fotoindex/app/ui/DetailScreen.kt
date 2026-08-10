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
import androidx.compose.foundation.layout.Arrangement
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.platform.LocalContext


@Composable
fun DetailScreen(onOpenDocument: (Long) -> Unit) {

    val viewModel: PhotoViewModel = viewModel()

    val context = LocalContext.current

    var searchText by remember {
        mutableStateOf(DetailState.searchText)
    }

    var searchInNotes by remember {
        mutableStateOf(DetailState.searchInNotes)
    }


    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showMultipleDeleteDialog by remember {
        mutableStateOf(false)
    }

    var selectedIds by remember {
        mutableStateOf(setOf<Long>())
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

        if (selectedIds.isNotEmpty()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {

                        val uris = ArrayList<android.net.Uri>()

                        viewModel.photos
                            .filter { it.id in selectedIds }
                            .forEach { photo ->

                                val firstFile = File(photo.firstPhoto)

                                if (firstFile.exists()) {

                                    uris.add(
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            firstFile
                                        )
                                    )

                                }

                                photo.secondPhoto?.let { secondPath ->

                                    val secondFile = File(secondPath)

                                    if (secondFile.exists()) {

                                        uris.add(
                                            FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.provider",
                                                secondFile
                                            )
                                        )

                                    }

                                }

                            }

                        if (uris.isNotEmpty()) {

                            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                                type = "image/jpeg"

                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    uris
                                )

                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Compartir fotografías"
                                )
                            )

                        }

                    }
                ) {
                    Text("Compartir")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showMultipleDeleteDialog = true
                    }
                ) {
                    Text("Borrar todas")
                }

            }
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

                        Checkbox(
                            checked = photo.id in selectedIds,
                            onCheckedChange = { checked ->

                                selectedIds =
                                    if (checked) {
                                        selectedIds + photo.id
                                    } else {
                                        selectedIds - photo.id
                                    }

                            }
                        )

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


    if (showMultipleDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showMultipleDeleteDialog = false
            },

            title = {
                Text("Eliminar documentos")
            },

            text = {
                Text(
                    "¿Seguro que desea eliminar " +
                            "${selectedIds.size} documentos seleccionados?"
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhotos(selectedIds)
                        selectedIds = emptySet()
                        showMultipleDeleteDialog = false
                    }
                ) {
                    Text("Eliminar")
                }

            },

            dismissButton = {

                Button(

                    onClick = {

                        showMultipleDeleteDialog = false

                    }

                ) {

                    Text("Cancelar")

                }

            }

        )

    }

}