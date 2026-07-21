package es.fotoindex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.systemBarsPadding
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay

@Composable
fun CameraScreen() {

    val context = LocalContext.current

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

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var firstPhotoPath by remember {
        mutableStateOf<String?>(null)
    }

    var secondPhotoPath by remember {
        mutableStateOf<String?>(null)
    }

    var previewPhotoPath by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(previewPhotoPath) {

        if (previewPhotoPath != null) {

            delay(4000)

            previewPhotoPath = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Cámara",
            style = MaterialTheme.typography.headlineMedium
        )

        if (hasCameraPermission) {

            val previewView = remember {
                PreviewView(context)
            }

            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(Unit) {

                CameraPreview.startCamera(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView
                )

                onDispose { }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                if (previewPhotoPath != null) {

                    AsyncImage(
                        model = previewPhotoPath,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                previewPhotoPath = null
                            },
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Toque la imagen para continuar",
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

        } else {

            Text(
                text = "Esperando permiso de cámara...",
                style = MaterialTheme.typography.bodyLarge
            )

        }

        Spacer(modifier = Modifier.height(16.dp))




        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                android.widget.Toast
                    .makeText(
                        context,
                        "Botón pulsado",
                        android.widget.Toast.LENGTH_SHORT
                    )
                    .show()

                CameraPreview.takePhoto(context) { photoPath ->

                    if (firstPhotoPath == null) {

                        firstPhotoPath = photoPath
                        previewPhotoPath = photoPath
                    } else {

                        secondPhotoPath = photoPath
                        previewPhotoPath = photoPath
                    }

                }

            }
        ) {
            Text("Tomar fotografía")
        }
    }
}