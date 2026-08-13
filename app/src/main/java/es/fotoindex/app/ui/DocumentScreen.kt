package es.fotoindex.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.AlertDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import java.io.File
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.relocation.BringIntoViewRequester
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import es.fotoindex.app.database.PhotoAttachment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import es.fotoindex.app.data.CaptureSessionState
import androidx.compose.ui.Alignment
import androidx.compose.material3.Checkbox

@Composable
fun DocumentScreen(

    navController: androidx.navigation.NavHostController,
    onBack: () -> Unit

) {
    val photo = SelectedDocument.photo ?: return
    val context = LocalContext.current
    val viewModel: PhotoViewModel = viewModel()
    val attachments =
        remember {
            mutableStateListOf<PhotoAttachment>()
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->

            uris.forEach { uri ->

                CameraPreview.copyGalleryImage(
                    context = context,
                    uri = uri
                ) { copiedPath ->

                    viewModel.addAttachment(
                        photoId = photo.id,
                        imagePath = copiedPath
                    )

                    attachments.add(
                        es.fotoindex.app.database.PhotoAttachment(
                            photoId = photo.id,
                            imagePath = copiedPath
                        )
                    )

                }

            }

        }



    LaunchedEffect(photo.id) {

        viewModel.loadAttachments(
            photo.id,
            attachments
        )

    }

    var notes by remember {
        mutableStateOf(photo.additionalText)
    }

    val focusRequester = remember { FocusRequester() }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showShareDialog by remember {
        mutableStateOf(false)
    }

    var shareMainPhotos by remember {
        mutableStateOf(true)
    }

    var shareNotes by remember {
        mutableStateOf(false)
    }

    var shareAdditionalPhotos by remember {
        mutableStateOf(false)
    }


    var showAddPhotoDialog by remember {
        mutableStateOf(false)
    }

    var selectedAttachment by remember {
        mutableStateOf<PhotoAttachment?>(null)
    }

    var showAttachmentDeleteDialog by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ){

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Lista de documentos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(4.dp))


        AsyncImage(
            model = photo.firstPhoto,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable {
                    openPhotoExternally(
                        context,
                        photo.firstPhoto
                    )
                },
            contentScale = ContentScale.Fit
        )

        photo.secondPhoto?.let {

            Spacer(Modifier.height(12.dp))

            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable {
                        openPhotoExternally(
                            context,
                            it
                        )
                    },
                contentScale = ContentScale.Fit

            )

        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {



            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    showShareDialog = true
                }
            ) {
                Text("Compartir")
            }


            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    showDeleteDialog = true
                }
            ) {
                Text("Eliminar")
            }



        }

        Spacer(Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showAddPhotoDialog = true
            }
        ) {
            Text("Añadir más fotos")
        }

        Spacer(Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                viewModel.updateNotes(
                    photo.id,
                    notes
                )

                android.widget.Toast.makeText(
                    context,
                    "Notas actualizadas",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

            }
        ) {
            Text("Modificar notas")
        }
        Spacer(Modifier.height(20.dp))

        Text(
            "Notas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(

            value = notes,

            onValueChange = {

                notes = it

            },

            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),

        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Fotos adicionales",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        if (attachments.isEmpty()) {

            Text(
                "No hay fotografías adicionales."
            )

        } else {

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                items(attachments) { attachment ->

                    AsyncImage(
                        model = attachment.imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clickable {

                                selectedAttachment = attachment

                            },

                        contentScale = ContentScale.Crop
                    )

                }

            }

        }





        Spacer(Modifier.height(20.dp))

        Text(
            "OCR",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            photo.ocrText
        )

        Spacer(Modifier.height(24.dp))


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
                Text("¿Seguro que desea eliminar este documento?")
            },

            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhoto(photo.id)
                        showDeleteDialog = false
                        onBack()
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


    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddPhotoDialog = false
            },

            title = {
                Text("Añadir fotografías")
            },

            text = {
                Text("Añadir imágenes desde:")
            },

            confirmButton = {
                Button(
                    onClick = {

                        showAddPhotoDialog = false

                        CaptureSessionState.additionalPhotoDocumentId = photo.id

                        es.fotoindex.app.image.ImageProvider.source =
                            es.fotoindex.app.image.ImageSource.CAMERA

                        navController.navigate(AppScreen.Camera.route)

                    }
                ) {
                    Text("📷 Cámara")
                }

            },

            dismissButton = {
                Button(
                    onClick = {

                        showAddPhotoDialog = false

                        galleryLauncher.launch("image/*")

                    }
                ) {
                    Text("🖼 Galería")
                }

            }

        )

    }


    if (selectedAttachment != null) {

        AlertDialog(

            onDismissRequest = {
                selectedAttachment = null
            },

            title = {
                Text("Imagen")
            },

            text = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    selectedAttachment?.let { attachment ->

                        AsyncImage(
                            model = attachment.imagePath,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(
                            Modifier.height(12.dp)
                        )

                    }

                    Text(
                        "¿Qué desea hacer con esta imagen?"
                    )

                }

            },

            confirmButton = {

                Button(
                    onClick = {

                        val attachment =
                            selectedAttachment

                        if (attachment != null) {

                            openPhotoExternally(
                                context,
                                attachment.imagePath
                            )

                        }

                        selectedAttachment = null

                    }
                ) {
                    Text("Ver")
                }

            },

            dismissButton = {

                Button(
                    onClick = {

                        showAttachmentDeleteDialog = true

                    }
                ) {
                    Text("Eliminar")
                }

            }

        )

    }


    if (showAttachmentDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showAttachmentDeleteDialog = false
                selectedAttachment = null
            },

            title = {
                Text("Eliminar imagen")
            },

            text = {
                Text("¿Está seguro de eliminar esta imagen?")
            },

            confirmButton = {
                Button(
                    onClick = {
                        selectedAttachment?.let { attachment ->

                            File(attachment.imagePath).delete()

                            viewModel.deleteAttachment(
                                attachment.id
                            )

                            attachments.remove(
                                attachment
                            )
                        }
                        showAttachmentDeleteDialog = false
                        selectedAttachment = null
                    }
                ) {
                    Text("Sí")
                }

            },

            dismissButton = {
                Button(
                    onClick = {
                        showAttachmentDeleteDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showShareDialog) {

        AlertDialog(

            onDismissRequest = {
                showShareDialog = false
            },

            title = {
                Text("Compartir documento")
            },

            text = {

                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = shareMainPhotos,
                            onCheckedChange = {
                                shareMainPhotos = it
                            }
                        )

                        Text("Primera y segunda foto")

                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = shareNotes,
                            onCheckedChange = {
                                shareNotes = it
                            }
                        )

                        Text("Notas")

                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = shareAdditionalPhotos,
                            onCheckedChange = {
                                shareAdditionalPhotos = it
                            }
                        )

                        Text("Fotos adicionales")

                    }

                }

            },

            confirmButton = {

                Button(
                    onClick = {

                        showShareDialog = false

                        val uris = ArrayList<android.net.Uri>()

                        val shareText = buildString {

                            if (shareMainPhotos) {
                                val firstFile = File(photo.firstPhoto)

                                uris.add(android.net.Uri.parse(photo.firstPhoto))
                                photo.secondPhoto?.let { secondPath ->
                                    val secondFile = File(secondPath)
                                    uris.add(android.net.Uri.parse(secondPath))
                                }

                            }

                            if (shareNotes) {
                                if (isNotEmpty()) {
                                    append("\n\n")
                                }
                                append("Notas")
                                append("\n\n")
                                append(notes)
                            }

                            if (shareAdditionalPhotos && attachments.isNotEmpty()) {

                                if (isNotEmpty()) {
                                    append("\n\n")
                                }

                                attachments.forEach { attachment ->

                                    uris.add(
                                        android.net.Uri.parse(
                                            attachment.imagePath
                                        )
                                    )
                                }
                            }
                        }

                        if (uris.isEmpty() && shareText.isBlank()) {
                            android.widget.Toast.makeText(
                                context,
                                "Seleccione al menos un elemento",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (uris.isEmpty()) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    shareText
                                )
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Compartir documento"
                                )
                            )

                        } else {

                            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                                type = "image/jpeg"

                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    uris
                                )

                                if (shareText.isNotBlank()) {

                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        shareText
                                    )

                                }

                                addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )

                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Compartir documento"
                                )
                            )

                        }

                    }
                ) {
                    Text("Compartir")
                }

            },




            dismissButton = {

                Button(
                    onClick = {
                        showShareDialog = false
                    }
                ) {
                    Text("Cancelar")
                }

            }

        )

    }





}

private fun openPhotoExternally(
    context: android.content.Context,
    photoPath: String
) {

    val uri = android.net.Uri.parse(photoPath)

    val intent = Intent(
        Intent.ACTION_VIEW
    ).apply {

        setDataAndType(
            uri,
            "image/*"
        )

        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
    }

    try {

        context.startActivity(intent)

    } catch (e: Exception) {

        android.widget.Toast.makeText(
            context,
            "No hay ninguna aplicación para visualizar esta imagen",
            android.widget.Toast.LENGTH_LONG
        ).show()

        e.printStackTrace()
    }
}