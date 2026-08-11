package es.fotoindex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(

    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenDocument: (Long) -> Unit

) {

    val photoViewModel: PhotoViewModel = viewModel()

    LaunchedEffect(Unit) {
        photoViewModel.loadPhotos()
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Pidem",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Lagoart'26-©",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Capturar + Galería en la misma línea

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Button(
                modifier = Modifier.weight(1f),
                onClick = onCaptureClick
            ) {
                Text("📷Capturar", fontSize = 13.sp)

            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onGalleryClick
            ) {
                Text("🖼Galería", fontSize =  13.sp)
            }

        }

        // Separación antes de Documentos

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSearchClick
        ) {
            Text("📚 Documentos")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSettingsClick
        ) {
            Text("⚙ Otras opciones")
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(

            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),

            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            items(photoViewModel.photos) { photo ->

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            SelectedDocument.photo = photo

                            onOpenDocument(photo.id)

                        }

                ) {

                    AsyncImage(

                        model = photo.firstPhoto,

                        contentDescription = null,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),

                        contentScale = ContentScale.Crop

                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = photo.ocrText.take(150)
                    )

                }

            }

        }

    }

}