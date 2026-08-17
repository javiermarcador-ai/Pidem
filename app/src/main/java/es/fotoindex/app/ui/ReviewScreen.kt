package es.fotoindex.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import es.fotoindex.app.data.ReviewData
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts



@Composable


fun ReviewScreen(

    onSave: (String) -> Unit

) {


    var additionalText by remember {
        mutableStateOf("")
    }

    var showDeleteDialog by remember {mutableStateOf(false)  }
    var deleteResultDialog by remember {mutableStateOf(false) }
    var deleteResultText by remember {mutableStateOf("") }

    val photoViewModel: PhotoViewModel = viewModel()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val galleryDeleteLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            val currentSession =
                ReviewData.session
            val galleryCount =
                currentSession
                    ?.originalImagePaths
                    ?.count {
                        CameraPreview.isGalleryImage(it)
                    }
                    ?: 0
            val galleryResult =
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    if (galleryCount == 1) {
                        "Imagen de galería borrada correctamente."
                    } else {
                        "$galleryCount imágenes de galería borradas correctamente."
                    }
                } else {
                    "No se pudieron eliminar las imágenes de la galería."
                }

            if (deleteResultText.isBlank()) {
                deleteResultText = galleryResult
            } else {
                deleteResultText =
                    deleteResultText +
                            "\n\n" +
                            galleryResult
            }
            deleteResultDialog = true
        }


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    Column(

        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Revisión del documento",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))


        if (showDeleteDialog) {

            androidx.compose.material3.AlertDialog(

                onDismissRequest = {
                    // Cancelar equivale a conservar las originales.
                    showDeleteDialog = false

                    ReviewData.session = null
                    onSave(additionalText)
                },

                title = {
                    Text("Eliminar imágenes originales")
                },

                text = {
                    Text(
                        "La imagen original ya ha sido incorporada a Pidem.\n\n" +
                                "¿Desea eliminar las imágenes del dispositivo para evitar tener copias duplicadas?"
                    )
                },

                confirmButton = {
                    Button(
                        onClick = {

                            val currentSession =
                                ReviewData.session

                            if (currentSession == null) {

                                showDeleteDialog = false
                                return@Button
                            }

                            val galleryPaths =
                                currentSession.originalImagePaths
                                    .filter {
                                        CameraPreview.isGalleryImage(it)
                                    }

                            val localResults =
                                currentSession.originalImagePaths
                                    .filter {
                                        !CameraPreview.isGalleryImage(it)
                                    }
                                    .map { path ->

                                        CameraPreview.deleteOriginalImage(
                                            context = context,
                                            path = path
                                        )
                                    }

                            val localDeleted =
                                localResults.count { it }

                            val localFailed =
                                localResults.count { !it }

                            /*
                             * Primero resolvemos las imágenes que
                             * podemos borrar directamente.
                             */

                            val localMessages =
                                mutableListOf<String>()

                            repeat(localDeleted) {

                                localMessages.add(
                                    "Imagen borrada correctamente."
                                )
                            }

                            repeat(localFailed) {

                                localMessages.add(
                                    "No se pudo eliminar la imagen."
                                )
                            }

                            /*
                             * Ahora gestionamos las imágenes procedentes
                             * de Gallery.
                             */

                            if (galleryPaths.isNotEmpty()) {

                                if (Build.VERSION.SDK_INT >= 30) {

                                    val pendingIntent =
                                        CameraPreview.createGalleryDeleteRequest(
                                            context = context,
                                            paths = galleryPaths
                                        )

                                    if (pendingIntent != null) {

                                        /*
                                         * Guardamos temporalmente el resultado
                                         * de cámara para mostrarlo después de
                                         * la autorización de Android.
                                         */
                                        deleteResultText =
                                            localMessages.joinToString(
                                                separator = "\n\n"
                                            )

                                        showDeleteDialog = false

                                        galleryDeleteLauncher.launch(
                                            IntentSenderRequest.Builder(
                                                pendingIntent.intentSender
                                            ).build()
                                        )

                                        return@Button
                                    }

                                } else {

                                    localMessages.add(
                                        "No se pueden eliminar las imágenes de la galería en esta versión de Android."
                                    )
                                }
                            }

                            /*
                             * Si no había imágenes de Gallery,
                             * mostramos directamente el resultado.
                             */

                            deleteResultText =
                                localMessages.joinToString(
                                    separator = "\n\n"
                                )

                            showDeleteDialog = false
                            deleteResultDialog = true
                        }
                    ) {
                        Text("Aceptar")
                    }
                },

                dismissButton = {

                    Button(
                        onClick = {

                            showDeleteDialog = false

                            ReviewData.session = null
                            onSave(additionalText)
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (deleteResultDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    deleteResultDialog = false
                    ReviewData.session = null
                    onSave(additionalText)
                },

                title = {                    Text("Resultado")                },
                text = {                    Text(deleteResultText)                },
                confirmButton = {
                    Button(
                        onClick = {
                            deleteResultDialog = false
                            ReviewData.session = null
                            onSave(additionalText)
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }




        ReviewData.session?.firstPhotoPath?.let {

            AsyncImage(
                model = it,
                contentDescription = "Primera fotografía",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))
        }

        ReviewData.session?.secondPhotoPath?.let {

            AsyncImage(
                model = it,
                contentDescription = "Segunda fotografía",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(

            modifier = Modifier.fillMaxWidth(),

            onClick = {

                android.widget.Toast.makeText(
                    context,
                    "Ha pulsado Guardar ",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                val session = ReviewData.session ?: return@Button

                android.widget.Toast.makeText(
                    context,
                    "Categoría al guardar: ${es.fotoindex.app.data.CaptureSessionState.selectedCategory}",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                photoViewModel.savePhoto(
                    firstPhoto = session.firstPhotoPath!!,
                    secondPhoto = session.secondPhotoPath,
                    ocrText = session.ocrText,
                    additionalText = additionalText,
                    category = session.category,

                    onSaved = {
                        if (session.originalImagePaths.isNotEmpty()) {
                            showDeleteDialog = true
                        } else {
                            ReviewData.session = null
                            onSave(additionalText)
                        }
                    }
                )

            }

        ) {

            Text("Guardar")

        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Notas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(

            value = additionalText,

            onValueChange = {
                additionalText = it
            },

            label = {Text("Notas")},
            placeholder = {Text("Escriba aquí sus observaciones...")},

            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OCR",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = ReviewData.session?.ocrText ?: "",
            style = MaterialTheme.typography.bodyMedium
        )


    }

}
