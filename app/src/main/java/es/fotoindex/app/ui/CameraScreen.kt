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

@Composable
fun CameraScreen(navController: androidx.navigation.NavHostController) {

    val context = LocalContext.current
    val viewModel: PhotoViewModel = viewModel()

    var session by remember {mutableStateOf(CaptureSession())  }


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
            GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                CameraPreview.copyGalleryImage(
                    context = context,
                    uri = uri
                ) { imagePath ->

                    OcrPipeline.process(
                        context = context,
                        imagePath = imagePath,

                        onSuccess = { text ->

                            if (session.reviewingFirstPhoto) {
                                session = session.copy(firstOcrText = text)

                            } else {
                                session = session.copy(secondOcrText = text)
                            }

                            session = session.copy(
                                previewPhotoPath = imagePath,
                                reviewingPhoto = true,
                                source = CaptureSource.GALLERY
                            )
                        },

                        onError = {

                            if (session.reviewingFirstPhoto) {
                                session = session.copy(firstOcrText = "")

                            } else {
                                session = session.copy(secondOcrText = "")
                            }

                            session = session.copy(
                                previewPhotoPath = imagePath,
                                reviewingPhoto = true,
                                source = CaptureSource.GALLERY
                            )
                        }

                    )
                }

            }

        }



    LaunchedEffect(Unit) {

        session = CaptureSession()

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
                    CameraPreview.takePhoto(context) { photoPath ->
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
            ) {

                Text(
                    if (CaptureSessionState.additionalPhotoDocumentId != null) {
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

                    val additionalPhotoDocumentId =
                        CaptureSessionState.additionalPhotoDocumentId

                    if (additionalPhotoDocumentId != null) {

                        viewModel.addAttachment(
                            photoId = additionalPhotoDocumentId,
                            imagePath = croppedPath
                        )

                        CaptureSessionState.additionalPhotoDocumentId = null

                        navController.popBackStack()

                    } else  if (session.reviewingFirstPhoto) {
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

}