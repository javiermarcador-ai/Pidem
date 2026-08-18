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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.material3.AlertDialog


@Composable
fun SettingsScreen(
    onCategoriesClick: () -> Unit,
    onDeleteAllFinished: () -> Unit
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
                // Próximamente: Exportar
            }
        ) {
            Text("📤 Exportar")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Próximamente: Importar
            }
        ) {
            Text("📥 Importar")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
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

            title = { Text("Eliminar todo")
            },

            text = {
                Text(
                    "Se va a proceder a borrar toda la información " +
                            "de Pidem: datos e imágenes. " +
                            "No se podrán recuperar. " +
                            "Esta operación es independiente a la " +
                            "desinstalación. ¿Está seguro?"
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



}