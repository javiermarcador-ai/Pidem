package es.fotoindex.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.fotoindex.app.data.ReviewData
import es.fotoindex.app.data.DetailState
import es.fotoindex.app.data.CaptureSessionState


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.route
    ) {

        composable(AppScreen.Home.route) {

            HomeScreen(
                onCaptureClick = {

                    CaptureSessionState.firstPhotoFromGallery = false

                    es.fotoindex.app.image.ImageProvider.source =
                        es.fotoindex.app.image.ImageSource.CAMERA

                    navController.navigate(AppScreen.Camera.route)

                },

                onSearchClick = {
                    DetailState.searchText = ""
                    DetailState.searchInNotes = true
                    navController.navigate(AppScreen.Detail.route)
                },

                onExportClick = {
                    navController.navigate(AppScreen.Settings.route)
                },

                onSettingsClick = {
                    navController.navigate(AppScreen.Settings.route)
                },

                onGalleryClick = {

                    CaptureSessionState.firstPhotoFromGallery = true

                    es.fotoindex.app.image.ImageProvider.source =
                        es.fotoindex.app.image.ImageSource.GALLERY

                    navController.navigate(AppScreen.Camera.route)

                },


                onOpenDocument = {
                    navController.navigate(AppScreen.Document.route)
                }
            )
        }

        composable(AppScreen.Camera.route) {
            CameraScreen(navController)
        }



        composable(AppScreen.Review.route) {

            ReviewScreen(

                onSave = {

                    navController.popBackStack(
                        AppScreen.Home.route,
                        false
                    )

                }

            )

        }



        composable(AppScreen.Detail.route) {

            DetailScreen(

                onOpenDocument = { id ->

                    navController.navigate(AppScreen.Document.route)

                }

            )

        }

        composable(AppScreen.Document.route) {

            DocumentScreen(
                navController = navController,
                onBack = {
                    navController.popBackStack()
                }
            )

        }

        composable(AppScreen.Settings.route) {
            SettingsScreen()
        }



    }
}