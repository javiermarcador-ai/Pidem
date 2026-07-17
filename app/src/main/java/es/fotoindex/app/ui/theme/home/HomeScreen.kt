package es.fotoindex.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Text(
            text = "FotoIndex",
            style = MaterialTheme.typography.headlineLarge
        )

        Button(
            modifier = Modifier.padding(top = 32.dp),
            onClick = { }
        ) {
            Text("📷 Capturar")
        }

        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { }
        ) {
            Text("🔍 Buscar")
        }

        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { }
        ) {
            Text("⚙ Ajustes")
        }

    }

}