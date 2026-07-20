package es.fotoindex.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                    navController.navigate(AppScreen.Camera.route)
                },

                onSearchClick = {
                    navController.navigate(AppScreen.Detail.route)
                },

                onExportClick = {
                    navController.navigate(AppScreen.Settings.route)
                },

                onImportClick = {
                    navController.navigate(AppScreen.Settings.route)
                },

                onSettingsClick = {
                    navController.navigate(AppScreen.Settings.route)
                }
            )
        }

        composable(AppScreen.Camera.route) {
            CameraScreen()
        }

        composable(AppScreen.Detail.route) {
            DetailScreen()
        }

        composable(AppScreen.Settings.route) {
            SettingsScreen()
        }
    }
}