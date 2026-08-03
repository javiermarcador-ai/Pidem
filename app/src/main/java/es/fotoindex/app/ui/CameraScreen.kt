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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import es.fotoindex.app.data.ReviewData
import es.fotoindex.app.model.CaptureSession
import es.fotoindex.app.crop.CropManager
import es.fotoindex.app.crop.CropResult
import es.fotoindex.app.crop.CropRequest
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import es.fotoindex.app.model.CaptureSource


@Composable
fun CameraScreen(navController: androidx.navigation.NavHostController) {

    val context = LocalContext.current

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

                    session = session.copy(

                        previewPhotoPath = imagePath,

                        reviewingPhoto = true,

                        source = CaptureSource.GALLERY

                    )
                }

            }

        }



    LaunchedEffect(Unit) {
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
                if (session.reviewingFirstPhoto)
                    "Primera fotografía"
                else
                    "Segunda fotografía",
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

                AsyncImage(
                    model = session.previewPhotoPath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!session.reviewingPhoto) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    CameraPreview.takePhoto(context) { photoPath ->

                        session = session.copy(
                            previewPhotoPath = photoPath,
                            reviewingPhoto = true
                        )

                    }

                }
            ) {

                Text(
                    if (session.reviewingFirstPhoto)
                        "Tomar primera fotografía"
                    else
                        "Tomar segunda fotografía"
                )
            }

        } else {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    session = session.copy(
                        previewPhotoPath = null,
                        reviewingPhoto = false
                    )
                }
            ) {

                Text("🔄 Repetir fotografía")

            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    CropManager.cropPhoto(

                        context = context,

                        request = CropRequest(

                            imagePath = session.previewPhotoPath!!

                        )

                    ) { result ->

                        when (result) {

                            is CropResult.Accepted -> {

                                if (session.reviewingFirstPhoto) {

                                    session = session.copy(

                                        firstPhotoPath = result.imagePath,

                                        previewPhotoPath = null,

                                        reviewingPhoto = false,

                                        showSecondPhotoDialog = true

                                    )

                                } else {

                                    session = session.copy(

                                        secondPhotoPath = result.imagePath,

                                        previewPhotoPath = null,

                                        reviewingPhoto = false

                                    )

                                    ReviewData.session = session.copy()

                                    navController.navigate(AppScreen.Review.route)

                                }

                            }

                            CropResult.Cancelled -> {

                                session = session.copy(

                                    previewPhotoPath = null,

                                    reviewingPhoto = false

                                )

                            }

                        }

                    }

                }
            ) {

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

                            reviewingFirstPhoto = false

                        )

                        es.fotoindex.app.image.ImageProvider.source =
                            es.fotoindex.app.image.ImageSource.GALLERY

                        galleryLauncher.launch("image/*")


                    }

                ) {
                    Text("Sí")
                }

            },

            dismissButton = {

                Button(
                    onClick = {
                        session = session.copy(showSecondPhotoDialog = false)

                        ReviewData.session = session.copy()

                        navController.navigate(AppScreen.Review.route)

                    }
                ) {
                    Text("No")
                }

            }

        )

    }

}