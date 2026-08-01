package es.fotoindex.app.ui

sealed class AppScreen(val route: String) {

    data object Home : AppScreen("home")

    data object Camera : AppScreen("camera")

    data object Gallery : AppScreen("gallery")

    data object Review : AppScreen("review")

    data object Detail : AppScreen("detail")

    data object Document : AppScreen("document")

    data object Settings : AppScreen("settings")
}