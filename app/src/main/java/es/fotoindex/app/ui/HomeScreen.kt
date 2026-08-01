package es.fotoindex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect


@Composable
fun HomeScreen(
    onCaptureClick: () -> Unit,
    onSearchClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    val photoViewModel: PhotoViewModel = viewModel()

    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "FotoIndex",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "FotoIndex-A.1909",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCaptureClick
        ) {
            Text("📷 Capturar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSearchClick
        ) {
            Text("📚 Documentos")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onExportClick
        ) {
            Text("📤 Exportar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onImportClick
        ) {
            Text("📥 Importar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSettingsClick
        ) {
            Text("⚙ Ajustes")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Últimas capturas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(photoViewModel.photos) { photo ->

                AsyncImage(
                    model = photo.firstPhoto,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = photo.ocrText.take(150)
                )

                Spacer(modifier = Modifier.height(20.dp))

            }

        }
    }
}