package es.fotoindex.app.ui


sealed class AppScreen(val route: String) {

    data object Home : AppScreen("home")

    data object Camera : AppScreen("camera")

    data object Detail : AppScreen("detail")

    data object Settings : AppScreen("settings")
}