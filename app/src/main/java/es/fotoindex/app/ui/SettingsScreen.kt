package es.fotoindex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalLocale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color


@Composable
fun SettingsScreen(
    onCategoriesClick: () -> Unit,
    onDeleteAllFinished: () -> Unit,
    onImportFinished: () -> Unit
) {

    val viewModel: PhotoViewModel = viewModel()

    var showFirstDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showSecondDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteErrorDialog by remember {
        mutableStateOf(false)
    }
    var showExportDialog by remember {
        mutableStateOf(false)
    }

    var showExportResultDialog by remember {
        mutableStateOf(false)
    }

    var showImportResultDialog by remember {
        mutableStateOf(false)
    }

    var importResultMessage by remember {
        mutableStateOf("")
    }

    var isImporting by remember {
        mutableStateOf(false)
    }

    var exportResultMessage by remember {
        mutableStateOf("")
    }

    var showImportConfirmDialog by remember {
        mutableStateOf(false)
    }

    var selectedImportUri by remember {
        mutableStateOf<android.net.Uri?>(null)
    }

    var selectedImportFileName by remember {
        mutableStateOf("")
    }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                val fileName =
                    uri.lastPathSegment
                        ?.substringAfterLast("/")
                        ?: ""

                if (
                    !fileName.endsWith(
                        ".eiP",
                        ignoreCase = true
                    )
                ) {

                    importResultMessage =
                        "El archivo seleccionado no es un archivo de Pidem (.eiP)."

                    showImportResultDialog = true

                } else {

                    selectedImportUri = uri

                    selectedImportFileName =
                        fileName

                    showImportConfirmDialog = true
                }
            }
        }

    val exportFileName =
        java.text.SimpleDateFormat(
            "yyyyMMddHHmm",
            LocalLocale.current.platformLocale
        ).format(
            java.util.Date()
        ) + "_datosPidem.eiP"


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            verticalArrangement = Arrangement.Top

        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Otras opciones",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showExportDialog = true
                }
            ) {
                Text("📤 Exportar")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting,
                onClick = {

                    importLauncher.launch(
                        arrayOf(
                            "*/*"
                        )
                    )
                }
            ) {
                Text("📥 Importar")
            }


            Spacer(modifier = Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting,
                onClick = onCategoriesClick
            ) {
                Text("📂 Gestionar categorías")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showFirstDeleteDialog = true
                }
            ) {
                Text("🗑 Eliminar Todo")
            }


        }

        if (showFirstDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showFirstDeleteDialog = false
                },

                title = {
                    Text("Eliminar todo")
                },

                text = {
                    Text(
                        "Se va a proceder a borrar toda la información de Pidem: \n\n" +
                                "          datos e imágenes. \n\n" +
                                "ATENCIÓN: No se podrán recuperar. \n\n" +
                                "Esta operación es independiente a la " +
                                "desinstalación. \n\n\n ¿Está seguro?"
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {

                            showFirstDeleteDialog = false
                            showSecondDeleteDialog = true

                        }
                    ) {
                        Text("Sí")
                    }
                },

                dismissButton = {

                    Button(
                        onClick = {
                            showFirstDeleteDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showSecondDeleteDialog) {

            AlertDialog(

                onDismissRequest = {
                    showSecondDeleteDialog = false
                },

                title = {
                    Text("Eliminar todo")
                },

                text = {
                    Text(
                        "Pulsando Adelante se eliminará todo " +
                                "lo almacenado en Pidem"
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {

                            showSecondDeleteDialog = false

                            viewModel.deleteAllData { success ->

                                if (success) {

                                    onDeleteAllFinished()

                                } else {

                                    showDeleteErrorDialog = true

                                }

                            }
                        }
                    ) {
                        Text("Adelante")
                    }
                },

                dismissButton = {

                    Button(
                        onClick = {
                            showSecondDeleteDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showDeleteErrorDialog) {

            AlertDialog(

                onDismissRequest = {
                    showDeleteErrorDialog = false
                },

                title = {
                    Text("No se pudo completar")
                },

                text = {
                    Text(
                        "Los datos de Pidem se han eliminado, " +
                                "pero no se pudo eliminar físicamente " +
                                "la carpeta de imágenes."
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {
                            showDeleteErrorDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        if (showExportDialog) {

            AlertDialog(

                onDismissRequest = {
                    showExportDialog = false
                },

                title = {
                    Text("Exportar datos")
                },

                text = {
                    Text(
                        "Si pulsa el botón Aceptar se iniciará " +
                                "el volcado de todas sus imágenes y " +
                                "datos de Pidem al archivo:\n\n" +
                                exportFileName
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {

                            showExportDialog = false

                            viewModel.exportData { result ->

                                exportResultMessage =
                                    if (result.isSuccess) {
                                        "Datos volcados y guardados en " +
                                                "/Download/" +
                                                result.getOrNull()
                                    } else {
                                        "No se pudo completar la exportación.\n\n" +
                                                "Motivo:\n" +
                                                result.exceptionOrNull()?.message
                                    }
                                showExportResultDialog = true
                            }
                        }
                    ) {
                        Text("Aceptar")
                    }
                },

                dismissButton = {

                    Button(
                        onClick = {
                            showExportDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        if (showExportResultDialog) {
            AlertDialog(
                onDismissRequest = { showExportResultDialog = false },
                title = {
                    Text(
                        if (exportResultMessage.startsWith(
                                "Datos volcados"
                            )
                        ) {
                            "Exportación completada"
                        } else {
                            "Error de exportación"
                        }
                    )
                },

                text = {
                    Text(exportResultMessage)
                },

                confirmButton = {

                    Button(
                        onClick = {
                            showExportResultDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }


        if (isImporting) {

            AlertDialog(
                onDismissRequest = {
                    // No permitimos cerrar el diálogo durante la importación
                },

                title = {
                    Text("Importando")
                },

                text = {

                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        CircularProgressIndicator(
                            modifier = Modifier.height(60.dp),
                            strokeWidth = 5.dp
                        )

                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                        Text(
                            "Importando datos e imágenes..."
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            "Por favor, espere."
                        )
                    }
                },

                confirmButton = {}
            )
        }


        if (showImportResultDialog) {
            AlertDialog(
                onDismissRequest = {
                    showImportResultDialog = false
                },
                title = {
                    Text(
                        if (
                            importResultMessage.startsWith(
                                "Importación completada"
                            )
                        ) {
                            "Importación completada"
                        } else {
                            "Error de importación"
                        }
                    )
                },

                text = {
                    Text(importResultMessage)
                },

                confirmButton = {

                    Button(
                        onClick = {

                            showImportResultDialog = false

                            if (
                                importResultMessage.startsWith(
                                    "Importación completada"
                                )
                            ) {
                                onImportFinished()
                            }

                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

    }


    if (showImportConfirmDialog) {

        AlertDialog(

            onDismissRequest = {
                showImportConfirmDialog = false
                selectedImportUri = null
            },

            title = {
                Text("Confirmar importación")
            },

            text = {
                Text(
                    "Se ha seleccionado el archivo:\n\n" +
                            selectedImportFileName
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showImportConfirmDialog = false

                        val uri =
                            selectedImportUri

                        if (uri != null) {

                            isImporting = true

                            viewModel.importData(
                                fileUri = uri,
                                replaceExisting = false
                            ) { result ->

                                isImporting = false

                                importResultMessage =
                                    if (result.isSuccess) {

                                        result.getOrNull()
                                            ?: "Importación completada"

                                    } else {

                                        "No se pudo completar la importación.\n\n" +
                                                "Motivo:\n" +
                                                (
                                                        result.exceptionOrNull()?.message
                                                            ?: "Error desconocido"
                                                        )
                                    }

                                showImportResultDialog = true
                                selectedImportUri = null
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },

            dismissButton = {

                Button(
                    onClick = {

                        showImportConfirmDialog = false
                        selectedImportUri = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

}