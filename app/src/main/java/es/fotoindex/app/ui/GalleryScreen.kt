package es.fotoindex.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
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
            OpenDocument()
        ) { uri: Uri? ->

            if (uri != null) {

                /*
                 * Conservamos la URI ORIGINAL.
                 *
                 * Ya NO hacemos una copia gallery_....jpg.
                 */
                try {

                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                } catch (e: Exception) {

                    /*
                     * Algunas aplicaciones/proveedores no
                     * permiten permisos persistentes.
                     *
                     * No es un error para continuar.
                     */
                    e.printStackTrace()
                }

                GalleryData.imagePath =
                    uri.toString()

                if (GalleryData.secondPhoto) {

                    GalleryData.secondPhoto = false

                    navController.popBackStack()

                } else {

                    navController.navigate(
                        AppScreen.Camera.route
                    ) {

                        popUpTo(
                            AppScreen.Home.route
                        )
                    }
                }

            } else {

                navController.popBackStack()

            }
        }

    LaunchedEffect(Unit) {

        galleryLauncher.launch(
            arrayOf("image/*")
        )
    }
}