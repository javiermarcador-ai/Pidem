package es.fotoindex.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import es.fotoindex.app.data.ReviewData
import androidx.lifecycle.viewmodel.compose.viewModel
import es.fotoindex.app.viewmodel.PhotoViewModel
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager

@Composable


fun ReviewScreen(

    onSave: (String) -> Unit

) {




    var additionalText by remember {
        mutableStateOf("")
    }


    val photoViewModel: PhotoViewModel = viewModel()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    Column(

        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(

            text = "Revisión del documento",

            style = MaterialTheme.typography.headlineSmall

        )
        Spacer(modifier = Modifier.height(20.dp))


        ReviewData.session?.firstPhotoPath?.let {

            AsyncImage(
                model = it,
                contentDescription = "Primera fotografía",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))
        }

        ReviewData.session?.secondPhotoPath?.let {

            AsyncImage(
                model = it,
                contentDescription = "Segunda fotografía",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(

            modifier = Modifier.fillMaxWidth(),

            onClick = {

                android.widget.Toast.makeText(
                    context,
                    "He pulsado Guardar",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                val session = ReviewData.session ?: return@Button

                photoViewModel.savePhoto(

                    firstPhoto = session.firstPhotoPath!!,

                    secondPhoto = session.secondPhotoPath,

                    ocrText = ReviewData.session?.ocrText ?: "",

                    additionalText = additionalText

                )

                ReviewData.session = null

                photoViewModel.loadPhotos()

                onSave(additionalText)

            }

        ) {

            Text("Guardar")

        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Notas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(

            value = additionalText,

            onValueChange = {
                additionalText = it
            },

            label = {Text("Notas")},
            placeholder = {Text("Escriba aquí sus observaciones...")},

            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OCR",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = ReviewData.session?.ocrText ?: "",
            style = MaterialTheme.typography.bodyMedium
        )


    }

}
