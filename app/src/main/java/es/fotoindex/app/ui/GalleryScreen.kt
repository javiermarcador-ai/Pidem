package es.fotoindex.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun GalleryScreen(
    navController: NavHostController
) {

    val context = LocalContext.current

    val galleryLauncher =
        rememberLauncherForActivityResult(
            GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                CameraPreview.copyGalleryImage(
                    context,
                    uri
                ) { path ->

                    android.widget.Toast.makeText(
                        context,
                        path,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center

    ) {

        Text(
            "Explorar galería",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(

            onClick = {

                galleryLauncher.launch("image/*")

            }

        ) {

            Text("Seleccionar fotografía")

        }

    }

}