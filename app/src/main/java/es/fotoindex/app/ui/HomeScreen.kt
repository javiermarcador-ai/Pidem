package es.fotoindex.app.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import es.fotoindex.app.data.PidemStorage
import es.fotoindex.app.viewmodel.PhotoViewModel

@Composable
fun HomeScreen(

    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenDocument: (Long) -> Unit

) {

    val context = LocalContext.current

    val photoViewModel: PhotoViewModel = viewModel()


    var showStorageDialog by rememberSaveable {

        mutableStateOf(
            PidemStorage.getStorageUri(context) == null
        )
    }

    /*
     * Selector de carpetas de Android.
     *
     * Cuando el usuario termina la selección,
     * Android devuelve la URI REAL sobre la que
     * el usuario ha concedido permiso.
     */

    val folderPickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocumentTree()
        ) { uri ->

            if (uri != null) {

                PidemStorage.saveStorageUri(
                    context,
                    uri
                )

                showStorageDialog = false
            }
        }



    LaunchedEffect(Unit) {

        photoViewModel.loadPhotos()
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally

    ) {

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Pidem",
            style =
                MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Lagoart '74-©   2026",
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /*
         * Capturar + Galería
         */
        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp)

        ) {

            Button(

                modifier =
                    Modifier.weight(1f),

                onClick =
                    onCaptureClick

            ) {

                Text(
                    "📷Capturar",
                    fontSize = 13.sp
                )
            }

            Button(

                modifier =
                    Modifier.weight(1f),

                onClick =
                    onGalleryClick

            ) {

                Text(
                    "🖼Galería",
                    fontSize = 13.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        /*
         * Documentos
         */
        Button(

            modifier =
                Modifier.fillMaxWidth(),

            onClick =
                onSearchClick

        ) {

            Text("📚 Documentos")
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        /*
         * Otras opciones
         */
        Button(

            modifier =
                Modifier.fillMaxWidth(),

            onClick =
                onSettingsClick

        ) {

            Text("⚙ Otras opciones")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /*
         * Lista de documentos
         */
        LazyColumn(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

            items(
                photoViewModel.photos
            ) { photo ->

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {

                                SelectedDocument.photo =
                                    photo

                                onOpenDocument(
                                    photo.id
                                )
                            }

                ) {

                    AsyncImage(

                        model =
                            photo.firstPhoto,

                        contentDescription =
                            null,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(90.dp),

                        contentScale =
                            ContentScale.Crop
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            photo.ocrText.take(150)
                    )
                }
            }
        }
    }

    /*
     * PRIMER ARRANQUE
     *
     * Este diálogo no permite cerrarse sin
     * seleccionar una carpeta.
     */
    if (showStorageDialog) {

        AlertDialog(

            onDismissRequest = {
                // No permitimos cerrar el aviso
                // sin seleccionar una carpeta.
            },

            title = {

                Text(
                    "Almacenamiento de imágenes"
                )
            },

            text = {

                Text(
                    "La carpeta que seleccione guardará " +
                            "las imágenes de Pidem.\n\n" +
                            "Si desea mantener la carpeta " +
                            "por defecto, en la siguiente " +
                            "ventana pulse «Usar esta carpeta» " +
                            "y conceda permiso."
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        folderPickerLauncher.launch(

                            PidemStorage
                                .getDefaultFolderUri(
                                    context
                                )
                        )
                    }

                ) {

                    Text(
                        "Seleccionar carpeta"
                    )
                }
            }
        )
    }
}