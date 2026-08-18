package es.fotoindex.app.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.res.painterResource
import es.fotoindex.app.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun HomeScreen(

    onCaptureClick: (String) -> Unit,
    onGalleryClick: (String) -> Unit,
    onSearchClick: (String) -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onOpenDocument: (Long) -> Unit

) {

    val context = LocalContext.current

    val photoViewModel: PhotoViewModel = viewModel()

    LaunchedEffect(Unit) {
        photoViewModel.loadCategoriesAndWait()
        photoViewModel.loadPhotos()
    }

    var showStorageDialog by rememberSaveable {

        mutableStateOf(
            PidemStorage.getStorageUri(context) == null
        )
    }

    var showAboutDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var categoryMenuExpanded by remember {
        mutableStateOf(false)
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




    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally

    ) {

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Pidem",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Lagoart® '74, 2026",
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            IconButton(
                onClick = {
                    showAboutDialog = true
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pidem_about),
                    contentDescription = "Información sobre Pidem",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        /*
 * CATEGORÍA
 *
 * "Todas" se muestra únicamente como referencia.
 * No se puede seleccionar desde Home porque
 * no es una categoría válida para guardar fotografías.
 */
        ExposedDropdownMenuBox(
            expanded = categoryMenuExpanded,
            onExpandedChange = {
                categoryMenuExpanded = !categoryMenuExpanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = photoViewModel.selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Categoría")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoryMenuExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = {
                    categoryMenuExpanded = false
                }
            ) {

/*
 * TODAS
 *
 * Es el filtro inicial y también puede
 * seleccionarse desde Home.
 */
                DropdownMenuItem(
                    text = {
                        Text("Todas")
                    },
                    onClick = {

                        photoViewModel.selectedCategory = "Todas"

                        photoViewModel.loadPhotos()

                        categoryMenuExpanded = false
                    }
                )

                /*
                 * CATEGORÍAS REALES
                 */
                photoViewModel.categories.forEach { category ->

                    DropdownMenuItem(
                        text = {
                            Text(category.name)
                        },
                        onClick = {

                            photoViewModel.selectedCategory =
                                category.name

                            photoViewModel.loadPhotos()

                            categoryMenuExpanded = false
                        }
                    )
                }

                /*
                 * GESTIÓN DE CATEGORÍAS
                 */
                DropdownMenuItem(
                    text = {
                        Text("+")
                    },
                    onClick = {

                        categoryMenuExpanded = false

                        onCategoriesClick()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                modifier = Modifier.weight(1f),
                onClick = {
                    onCaptureClick (photoViewModel.selectedCategory)
                }
            ) {
                Text(
                    "📷Capturar",
                    fontSize = 13.sp
                )
            }


            Button(
                modifier =
                    Modifier.weight(1f),
                onClick = {
                    onGalleryClick(
                        photoViewModel.selectedCategory
                    )
                }
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
            onClick = {
                onSearchClick(
                    photoViewModel.selectedCategory
                )
            }
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


    /*
     * INFORMACIÓN SOBRE PIDEM
     */
    if (showAboutDialog) {

        AlertDialog(

            onDismissRequest = {
                showAboutDialog = false
            },

            title = {

                Text(
                    text = "Pidem",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            },

            text = {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Proyecto de Imáganes y Documentos Electrónicos para Móvil.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer( modifier = Modifier.height(16.dp) )

                    Text(
                        text = buildAnnotatedString {
                            append("Desarrollado por ")
                            withStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                            ) {
                                append("Lagoart®-'74")
                            }
                            append(" 2026")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Text(
                        text = "Si descubriste algún error o quieres proponer " +
                                "alguna mejora, escríbeme a:",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "javier.marcador@gmail.com",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                val intent = Intent(
                                    Intent.ACTION_SENDTO
                                ).apply {

                                    data = android.net.Uri.parse(
                                        "mailto:javier.marcador@gmail.com"
                                    )
                                }

                                try {

                                    context.startActivity(intent)

                                } catch (e: Exception) {

                                    android.widget.Toast.makeText(
                                        context,
                                        "No hay ninguna aplicación de correo configurada",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6A5ACD),
                        fontStyle = FontStyle.Italic,
                        textDecoration = TextDecoration.Underline
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showAboutDialog = false
                    }
                ) {

                    Text("Cerrar")
                }
            }
        )
    }
}