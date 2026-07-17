package es.fotoindex.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import es.fotoindex.app.ui.home.HomeScreen

@Composable
fun FotoIndexApp() {
    MaterialTheme {
        Surface {
            HomeScreen()
        }
    }
}