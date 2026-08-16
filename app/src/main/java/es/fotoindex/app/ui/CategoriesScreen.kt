package es.fotoindex.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.database.Category
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun CategoriesScreen(
    onBack: () -> Unit
) {

    val viewModel: PhotoViewModel = viewModel()

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var categoryToEdit by remember {
        mutableStateOf<Category?>(null)
    }

    var categoryToDelete by remember {
        mutableStateOf<Category?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {

        Text(
            text = "Gestionar categorías",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            items(
                viewModel.categories
            ) { category ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = category.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    )

                    /*
                     * DOCUMENTOS
                     *
                     * Es una categoría protegida.
                     * No permitimos eliminarla.
                     */
                    if (category.name != "Documentos") {

                        IconButton(
                            onClick = {
                                categoryToEdit = category
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modificar categoría"
                            )
                        }

                        IconButton(
                            onClick = {
                                categoryToDelete = category
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar categoría"
                            )
                        }

                    } else {

                        Text(
                            text = "Protegida",
                            modifier = Modifier.padding(
                                vertical = 12.dp
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showAddDialog = true
            }
        ) {
            Text("+ Añadir categoría")
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Volver")
        }
    }

    /*
     * AÑADIR CATEGORÍA
     */
    if (showAddDialog) {

        var newCategoryName by remember {
            mutableStateOf("")
        }

        AlertDialog(

            onDismissRequest = {
                showAddDialog = false
            },

            title = {
                Text("Nueva categoría")
            },

            text = {

                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = {
                        newCategoryName = it
                    },
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

            },

            confirmButton = {

                Button(
                    onClick = {

                        val name =
                            newCategoryName.trim()

                        if (
                            name.isNotEmpty() &&
                            !name.equals(
                                "Todas",
                                ignoreCase = true
                            ) &&
                            !name.equals(
                                "Documentos",
                                ignoreCase = true
                            ) &&
                            viewModel.categories.none {
                                it.name.equals(
                                    name,
                                    ignoreCase = true
                                )
                            }
                        ) {

                            viewModel.insertCategory(name)

                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Añadir")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showAddDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    /*
     * MODIFICAR CATEGORÍA
     */
    categoryToEdit?.let { category ->

        var newName by remember(category.id) {
            mutableStateOf(category.name)
        }

        AlertDialog(

            onDismissRequest = {
                categoryToEdit = null
            },

            title = {
                Text("Modificar categoría")
            },

            text = {

                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                    },
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        val name =
                            newName.trim()

                        if (
                            name.isNotEmpty() &&
                            !name.equals(
                                "Todas",
                                ignoreCase = true
                            ) &&
                            !name.equals(
                                "Documentos",
                                ignoreCase = true
                            ) &&
                            name != "+" &&
                            viewModel.categories.none {
                                it.name.equals(
                                    name,
                                    ignoreCase = true
                                )
                            }
                        )  {

                            viewModel.updateCategory(
                                category.id,
                                name
                            )

                            categoryToEdit = null
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        categoryToEdit = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    /*
     * BORRAR CATEGORÍA
     */
    categoryToDelete?.let { category ->

        AlertDialog(

            onDismissRequest = {
                categoryToDelete = null
            },

            title = {
                Text("Eliminar categoría")
            },

            text = {

                Text(
                    "¿Está seguro de que desea eliminar " +
                            "la categoría \"${category.name}\"?\n\n" +
                            "Los documentos que tengan esta categoría " +
                            "no se modificarán."
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        viewModel.deleteCategory(
                            category.id
                        )

                        categoryToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

