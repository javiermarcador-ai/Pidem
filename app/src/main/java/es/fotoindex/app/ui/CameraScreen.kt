package es.fotoindex.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.fotoindex.app.data.ReviewData
import es.fotoindex.app.model.CaptureSession
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.model.CaptureSource
import es.fotoindex.app.ocr.OcrPipeline
import es.fotoindex.app.data.CaptureSessionState
import es.fotoindex.app.database.PhotoAttachment
import es.fotoindex.app.viewmodel.PhotoViewModel
import android.content.Intent

@Composable
fun CameraScreen(navController: androidx.navigation.NavHostController) {

    val context = LocalContext.current
    val viewModel: PhotoViewModel = viewModel()

    var session by remember {
        mutableStateOf(
            CaptureSession(
                category = CaptureSessionState.selectedCategory
            )
        )
    }

    var galleryCancelled by remember {
        mutableStateOf(false)
    }

    var showDeleteCameraOriginalDialog by remember {
        mutableStateOf(false)
    }

    var cameraOriginalPathToDelete by remember {
        mutableStateOf<String?>(null)
    }



    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = GetContent()
        ) { uri: Uri? ->

            if (uri == null) {      galleryCancelled = true

            } else {
                galleryCancelled = false
                /*
                 * Guardamos la URI ORIGINAL de la galería.
                 *
                 * Ya no hacemos una copia temporal gallery_XXXX.jpg.
                 */

                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Algunos proveedores no permiten permisos persistentes.
                }

                val originalPath = uri.toString()
                val updatedOriginals = session.originalImagePaths + originalPath
                session = session.copy(
                    originalImagePaths = updatedOriginals,
                    temporaryGalleryPath = null
                )

                /*
                 * Procesamos directamente la imagen original.
                 */
                OcrPipeline.process(
                    context = context,
                    imagePath = originalPath,
                    onSuccess = { text ->

                        if (session.reviewingFirstPhoto) {
                            session = session.copy( firstOcrText = text )
                        } else {
                            session = session.copy(secondOcrText = text )
                        }

                        session = session.copy(
                                previewPhotoPath = originalPath,
                                reviewingPhoto = true,
                                source = CaptureSource.GALLERY
                            )
                    },

                    onError = {
                        if (session.reviewingFirstPhoto) {
                            session = session.copy(firstOcrText = "")

                        } else {

                            session =
                                session.copy(
                                    secondOcrText = ""
                                )
                        }

                        session =
                            session.copy(
                                previewPhotoPath = originalPath,
                                reviewingPhoto = true,
                                source = CaptureSource.GALLERY
                            )
                    }
                )
            }
        }


    LaunchedEffect(Unit) {

        session = CaptureSession(
            category = CaptureSessionState.selectedCategory
        )

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    LaunchedEffect(Unit) {


        if (es.fotoindex.app.image.ImageProvider.source ==
            es.fotoindex.app.image.ImageSource.GALLERY) {

            galleryLauncher.launch("image/*")

        }

    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text =
                if (CaptureSessionState.additionalPhotoDocumentId != null) {
                    "Fotografía adicional"
                } else if (session.reviewingFirstPhoto) {
                    "Primera fotografía"
                } else {
                    "Segunda fotografía"
                },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasCameraPermission) {

            Text(
                text = "Esperando permiso de cámara...",
                style = MaterialTheme.typography.bodyLarge
            )

            return@Column
        }

        val previewView = remember {
            PreviewView(context)
        }

        val lifecycleOwner = LocalLifecycleOwner.current

        if (es.fotoindex.app.image.ImageProvider.source ==
            es.fotoindex.app.image.ImageSource.CAMERA) {

            DisposableEffect(Unit) {

                CameraPreview.startCamera(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView
                )

                onDispose { }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            if (!session.reviewingPhoto) {

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

            } else {

                CropImageView(

                    imagePath = session.previewPhotoPath!!,

                    cropArea = CropState.cropArea

                )

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!session.reviewingPhoto) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (galleryCancelled) {

                        navController.popBackStack()

                    } else {

                        CameraPreview.takePhoto(context) { photoPath ->

                            session = session.copy(
                                originalImagePaths =
                                    session.originalImagePaths + photoPath
                            )

                            OcrPipeline.process(
                                context = context,
                                imagePath = photoPath,
                                onSuccess = { text ->
                                    session = if (session.reviewingFirstPhoto) {
                                        session.copy(
                                            previewPhotoPath = photoPath,
                                            reviewingPhoto = true,
                                            firstOcrText = text
                                        )

                                    } else {
                                        session.copy(
                                            previewPhotoPath = photoPath,
                                            reviewingPhoto = true,
                                            secondOcrText = text
                                        )
                                    }
                                },


                                onError = {

                                    session = if (session.reviewingFirstPhoto) {

                                        session.copy(
                                            previewPhotoPath = photoPath,
                                            reviewingPhoto = true,
                                            firstOcrText = ""
                                        )

                                    } else {

                                        session.copy(
                                            previewPhotoPath = photoPath,
                                            reviewingPhoto = true,
                                            secondOcrText = ""
                                        )

                                    }

                                },

                                )
                        }

                    }

                }
            ) {

                Text(
                    if (galleryCancelled) {
                        "Cerrar"
                    } else if (CaptureSessionState.additionalPhotoDocumentId != null) {
                        "Tomar fotografía"
                    } else if (session.reviewingFirstPhoto) {
                        "Tomar primera fotografía"
                    } else {
                        "Tomar segunda fotografía"
                    }
                )
            }

        } else {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    if (session.source == CaptureSource.GALLERY) {
                        // La fotografía actual procede de la galería.
                        // Volvemos a abrir el selector para elegir otra.
                        session = session.copy(
                            previewPhotoPath = null,
                            reviewingPhoto = false
                        )

                        galleryLauncher.launch("image/*")

                    } else {
                        // La fotografía actual procede de la cámara.
                        // Volvemos al visor de la cámara.
                        session = session.copy(
                            previewPhotoPath = null,
                            reviewingPhoto = false
                        )
                        es.fotoindex.app.image.ImageProvider.source =
                            es.fotoindex.app.image.ImageSource.CAMERA
                    }
                }
            ) {
                Text("🔄 Repetir fotografía")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val croppedPath = ImageCropper.processCrop(
                        context = context,
                        bitmap = CropBitmapState.bitmap!!,
                        cropArea = CropState.cropArea
                    )

                    val temporaryGalleryPath = session.temporaryGalleryPath

                    if (temporaryGalleryPath != null) {

                        CameraPreview.deleteImage(
                            context = context,
                            path = temporaryGalleryPath
                        )

                        session = session.copy(
                            temporaryGalleryPath = null
                        )
                    }

                    val additionalPhotoDocumentId =
                        CaptureSessionState.additionalPhotoDocumentId

                    if (additionalPhotoDocumentId != null) {

                        /*
                         * Estamos añadiendo una fotografía adicional
                         * tomada con la cámara.
                         *
                         * photoPath = fotografía original capturada
                         * croppedPath = fotografía que realmente
                         *               incorporamos al documento.
                         */
                        val originalCameraPath = session.previewPhotoPath

                        viewModel.addAttachment(
                            photoId = additionalPhotoDocumentId,
                            imagePath = croppedPath
                        )
                        CaptureSessionState.additionalPhotoDocumentId = null

                        /*
                         * Si tenemos la fotografía original,
                         * preguntamos al usuario si desea eliminarla.
                         */
                        if (originalCameraPath != null) {
                            cameraOriginalPathToDelete = originalCameraPath
                            showDeleteCameraOriginalDialog = true

                        } else {
                            navController.popBackStack()
                        }

                    } else if (session.reviewingFirstPhoto) {
                            session = session.copy(
                                firstPhotoPath = croppedPath,
                                previewPhotoPath = null,
                                reviewingPhoto = false,
                                showSecondPhotoDialog = true
                            )

                            } else {
                                session = session.copy(
                                    secondPhotoPath = croppedPath,
                                    previewPhotoPath = null,
                                    reviewingPhoto = false
                                )

                                ReviewData.session = session.copy(
                                    ocrText = buildString {
                                        append(session.firstOcrText)
                                        if (session.secondOcrText.isNotBlank()) {
                                            append("\n\n")
                                            append(session.secondOcrText)
                                        }
                                    }
                                )

                                /* android.util.Log.d(
                                    "FotoIndex",
                                    "Cargando fotografías..."
                                )*/

                                navController.navigate(AppScreen.Review.route)

                            }

                    }
            )  {
                Text("✅ Aceptar fotografía")
            }

        }

    }

    if (session.showSecondPhotoDialog) {

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                session = session.copy(showSecondPhotoDialog = false)
            },

            title = {
                Text("Segunda fotografía")
            },

            text = {
                Text("¿Desea realizar una segunda fotografía?")
            },

            confirmButton = {

                Button(
                    onClick = {

                        session = session.copy(
                            showSecondPhotoDialog = false,
                            reviewingFirstPhoto = false,
                            reviewingPhoto = false,
                            previewPhotoPath = null
                        )

                        if (CaptureSessionState.firstPhotoFromGallery) {

                            galleryLauncher.launch("image/*")

                        } else {

                            es.fotoindex.app.image.ImageProvider.source =
                                es.fotoindex.app.image.ImageSource.CAMERA

                        }

                    }
                ) {
                    Text("Sí")
                }
            },

            dismissButton = {

                Button(
                    onClick = {
                        session = session.copy(showSecondPhotoDialog = false)

                        ReviewData.session = session.copy(

                            ocrText = buildString {

                                append(session.firstOcrText)

                                if (session.secondOcrText.isNotBlank()) {

                                    append("\n\n")

                                    append(session.secondOcrText)

                                }

                            }

                        )

                        navController.navigate(AppScreen.Review.route)

                    }
                ) {
                    Text("No")
                }

            }

        )

    }

    if (showDeleteCameraOriginalDialog) {

        androidx.compose.material3.AlertDialog(

            onDismissRequest = {

                showDeleteCameraOriginalDialog = false
                cameraOriginalPathToDelete = null

                navController.popBackStack()
            },

            title = {
                Text("Eliminar fotografía original")
            },

            text = {
                Text(
                    "La fotografía original tomada con la cámara " +
                            "ya ha sido incorporada a Pidem mediante " +
                            "la fotografía recortada.\n\n" +
                            "¿Desea eliminar la fotografía original " +
                            "para evitar tener una copia duplicada?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        val originalPath =
                            cameraOriginalPathToDelete

                        if (originalPath != null) {

                            val deleted =
                                CameraPreview.deleteOriginalImage(
                                    context = context,
                                    path = originalPath
                                )

                            if (deleted) {

                                android.widget.Toast.makeText(
                                    context,
                                    "Fotografía original eliminada",
                                     android.widget.Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                android.widget.Toast.makeText(
                                    context,
                                    "No se pudo eliminar la fotografía original",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        showDeleteCameraOriginalDialog = false
                        cameraOriginalPathToDelete = null

                        navController.popBackStack()
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {

                Button(
                    onClick = {

                        /*
                         * No se elimina la original.
                         * Simplemente volvemos al documento.
                         */

                        showDeleteCameraOriginalDialog = false
                        cameraOriginalPathToDelete = null

                        navController.popBackStack()
                    }
                ) {
                    Text("Conservar")
                }
            }
        )
    }


}