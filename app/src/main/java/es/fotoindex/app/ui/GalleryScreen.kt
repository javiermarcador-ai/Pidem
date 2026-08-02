package es.fotoindex.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import es.fotoindex.app.data.GalleryData

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
                ) { imagePath ->

                    GalleryData.imagePath = imagePath

                    if (GalleryData.secondPhoto) {

                        GalleryData.secondPhoto = false

                        navController.popBackStack()

                    } else {

                        navController.navigate(AppScreen.Camera.route) {

                            popUpTo(AppScreen.Home.route)

                        }

                    }

                }

            } else {

                navController.popBackStack()

            }

        }

    LaunchedEffect(Unit) {

        galleryLauncher.launch("image/*")

    }

}