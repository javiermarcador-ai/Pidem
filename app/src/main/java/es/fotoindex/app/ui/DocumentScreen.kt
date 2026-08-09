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
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable


@Composable
fun DocumentScreen(

    onBack: () -> Unit

) {
    val photo = SelectedDocument.photo ?: return
    val context = LocalContext.current
    val viewModel: PhotoViewModel = viewModel()

    var notes by remember {
        mutableStateOf(photo.additionalText)
    }

    val focusRequester = remember { FocusRequester() }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember {

        mutableStateOf(false)

    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())


    ) {

        AsyncImage(
            model = photo.firstPhoto,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clickable {
                    openPhotoExternally(
                        context,
                        photo.firstPhoto
                    )
                },
            contentScale = ContentScale.Fit
        )

        photo.secondPhoto?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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

    /*                android.util.Log.d(
                        "SHARE_TEST",
                        "firstPhoto=${photo.firstPhoto}"
                    )

                    android.util.Log.d(
                        "SHARE_TEST",
                        "firstExists=${File(photo.firstPhoto).exists()}"
                    )
*/
                    val uris = ArrayList<android.net.Uri>()
                    val firstFile = File(photo.firstPhoto)

                    uris.add(
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            firstFile
                        )
                    )

                    photo.secondPhoto?.let {

                        val secondFile = File(it)

                        uris.add(
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                secondFile
                            )
                        )

                    }

                    val intent =
                        if (uris.size == 1) {

                            Intent(Intent.ACTION_SEND).apply {

                                type = "image/jpeg"

                                putExtra(Intent.EXTRA_STREAM, uris[0])

                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            }

                        } else {

                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                                type = "image/jpeg"

                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    uris
                                )

                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            }

                        }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Compartir fotografías"
                        )
                    )

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
        Spacer(Modifier.height(12.dp))

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
            "OCR",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(4.dp))

        Text(photo.ocrText)


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

}

private fun openPhotoExternally(
    context: android.content.Context,
    photoPath: String
) {

    val file = File(photoPath)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent().apply {

        action = Intent.ACTION_VIEW
        setDataAndType(uri, "image/jpeg")

        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
    }

    android.util.Log.d(
        "OPEN_PHOTO",
        "Abriendo: $uri"
    )


    context.startActivity(intent)



}