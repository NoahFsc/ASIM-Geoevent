package fr.miage.geotrouvetou.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.appViewModelFactory
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ImageUploader
import fr.miage.geotrouvetou.ui.components.atoms.Input
import fr.miage.geotrouvetou.ui.components.atoms.Switch
import fr.miage.geotrouvetou.ui.components.atoms.TextArea
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.molecules.PlaceSearchBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventUpdateScreen(
    event: Evenement,
    onBackClick: () -> Unit,
    onEventUpdated: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EventUpdateViewModel = viewModel(
        key = event.id,
        factory = appViewModelFactory(context),
    )

    LaunchedEffect(event) {
        viewModel.setEvent(event)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    val timePickerState = rememberTimePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                        viewModel.date = formatter.format(date)
                    }
                    showDatePicker = false
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .background(colorResource(R.color.white), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choisir l'heure",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker)
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTimePicker = false }) { Text("Annuler") }
                    TextButton(onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        viewModel.time = "$hour:$minute"
                        showTimePicker = false
                    }) { Text("Confirmer") }
                }
            }
        }
    }

    if (errorMessage != null) {
        Toast(
            title = "Erreur",
            description = errorMessage!!,
            duration = 3000
        )
        LaunchedEffect(errorMessage) {
            delay(3500)
            errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventUpdated.collect {
            onEventUpdated()
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { message ->
            errorMessage = message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .clickable { onBackClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = colorResource(R.color.text_light),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Retour",
                fontSize = 18.sp,
                color = colorResource(R.color.text_light)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Modifier l'événement",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.primary_600),
                lineHeight = 34.sp
            )

            ImageUploader(
                imageUri = viewModel.imageUri,
                onImageSelected = { viewModel.imageUri = it },
                label = "Image de couverture",
                required = true,
                imageUrl = viewModel.currentImageUrl
            )

            Input(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                placeholder = "Titre",
                label = "Titre",
                required = true,
            )

            TextArea(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                placeholder = "Description",
                label = "Description",
                maxLength = 500,
                required = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Input(
                    value = viewModel.date,
                    onValueChange = {},
                    placeholder = "dd/mm/yyyy",
                    label = "Date",
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true },
                    readOnly = true
                )
                Input(
                    value = viewModel.time,
                    onValueChange = {},
                    placeholder = "HH:mm",
                    label = "Heure",
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showTimePicker = true },
                    readOnly = true
                )
            }

            Input(
                value = viewModel.location,
                onValueChange = { viewModel.location = it },
                placeholder = "Localisation",
                label = "Localisation",
                required = true,
                leadingIcon = Icons.Default.Search
            )

            PlaceSearchBar(
                query = viewModel.location,
                onPlaceSelected = { place ->
                    viewModel.latitude = place.latitude
                    viewModel.longitude = place.longitude
                    viewModel.location = place.displayName
                },
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Type d'événement",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker),
                )
                Switch(
                    checked = viewModel.isPrivate,
                    onCheckedChange = { viewModel.isPrivate = it },
                    label = "Rendre l'événement privé"
                )
            }

            Button(
                text = if (viewModel.isLoading) "Modification..." else "Enregistrer les modifications",
                onClick = {
                    val bytes = viewModel.imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    viewModel.updateEvent(bytes)
                },
                enabled = viewModel.isFormValid,
                leftIcon = if (viewModel.isLoading) null else Icons.Default.Check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
