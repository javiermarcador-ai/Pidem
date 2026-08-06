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


@Composable
fun DocumentScreen(

    onBack: () -> Unit

) {
    val photo = SelectedDocument.photo ?: return
    val context = LocalContext.current
    val viewModel: PhotoViewModel = viewModel()

    var showDeleteDialog by remember {

        mutableStateOf(false)

    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())

    ) {

        AsyncImage(

            model = photo.firstPhoto,

            contentDescription = null,

            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),

            contentScale = ContentScale.Fit

        )

        photo.secondPhoto?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(

                model = it,

                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),

                contentScale = ContentScale.Fit

            )

        }

        Spacer(Modifier.height(24.dp))

        Text(

            text = "OCR",

            style = MaterialTheme.typography.titleMedium

        )

        Spacer(Modifier.height(8.dp))

        Text(photo.ocrText)

        Spacer(Modifier.height(24.dp))

        Text(

            text = "Notas",

            style = MaterialTheme.typography.titleMedium

        )

        Spacer(Modifier.height(8.dp))

        Text(photo.additionalText)

        Spacer(Modifier.height(24.dp))

        Button(

            modifier = Modifier.fillMaxWidth(),
            onClick = {
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

                val intent = if (uris.size == 1) {

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

        Spacer(Modifier.height(12.dp))

        Button(

            modifier = Modifier.fillMaxWidth(),

            onClick = {

                showDeleteDialog = true

            }

        ) {

            Text("Eliminar")

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