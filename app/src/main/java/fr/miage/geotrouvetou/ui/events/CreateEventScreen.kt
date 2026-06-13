package fr.miage.geotrouvetou.ui.events

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Place
import fr.miage.geotrouvetou.ui.appViewModelFactory
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ImageUploader
import fr.miage.geotrouvetou.ui.components.atoms.Input
import fr.miage.geotrouvetou.ui.components.atoms.Switch
import fr.miage.geotrouvetou.ui.components.atoms.TextArea
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.molecules.PlaceSearchBar
import fr.miage.geotrouvetou.ui.components.organisms.SearchBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onEventCreated: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: CreateEventViewModel = viewModel(factory = appViewModelFactory(context))

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberFutureDatePickerState()
    val timePickerState = rememberTimePickerState()

    EventDateTimePickers(
        showDatePicker = showDatePicker,
        showTimePicker = showTimePicker,
        datePickerState = datePickerState,
        timePickerState = timePickerState,
        onDismissDatePicker = { showDatePicker = false },
        onDismissTimePicker = { showTimePicker = false },
        onDateSelected = { viewModel.date = it },
        onTimeSelected = { viewModel.time = it },
    )

    if (errorMessage != null) {
        Toast(
            title = stringResource(R.string.event_error_title),
            description = errorMessage!!,
            duration = 3000
        )
        LaunchedEffect(errorMessage) {
            delay(3500)
            errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventCreated.collect {
            onEventCreated()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { message ->
            errorMessage = message
        }
    }

    CreateEventContent(
        title = viewModel.title,
        onTitleChange = { viewModel.title = it },
        description = viewModel.description,
        onDescriptionChange = { viewModel.description = it },
        date = viewModel.date,
        onDateClick = { showDatePicker = true },
        time = viewModel.time,
        onTimeClick = { showTimePicker = true },
        location = viewModel.location,
        onLocationChange = { viewModel.location = it },
        onPlaceSelected = { place ->
            viewModel.latitude = place.latitude
            viewModel.longitude = place.longitude
            viewModel.location = place.shortAddress
        },
        isPrivate = viewModel.isPrivate,
        onPrivateChange = { viewModel.isPrivate = it },
        imageUri = viewModel.imageUri,
        onImageSelected = { viewModel.imageUri = it },
        isLoading = viewModel.isLoading,
        isFormValid = viewModel.isFormValid,
        onCreateEvent = {
            val bytes = viewModel.imageUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            viewModel.createEvent(bytes)
        }
    )
}

@Composable
fun CreateEventContent(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    date: String,
    onDateClick: () -> Unit,
    time: String,
    onTimeClick: () -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    isLoading: Boolean,
    isFormValid: Boolean,
    onCreateEvent: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = stringResource(R.string.create_event_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorResource(R.color.text_darker),
        )

        ImageUploader(
            imageUri = imageUri,
            onImageSelected = onImageSelected,
            label = stringResource(R.string.event_form_cover_image),
            required = true,
        )

        Input(
            value = title,
            onValueChange = onTitleChange,
            placeholder = stringResource(R.string.event_form_title_placeholder),
            label = stringResource(R.string.event_form_title_label),
            required = true,
        )

        TextArea(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = stringResource(R.string.event_form_description_placeholder),
            label = stringResource(R.string.event_form_description_label),
            maxLength = 500,
            required = true,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Input(
                value = date,
                onValueChange = {},
                placeholder = stringResource(R.string.event_form_date_placeholder),
                label = stringResource(R.string.event_form_date_label),
                required = true,
                modifier = Modifier.weight(1f),
                onClick = onDateClick,
                readOnly = true
            )
            Input(
                value = time,
                onValueChange = {},
                placeholder = stringResource(R.string.event_form_time_placeholder),
                label = stringResource(R.string.event_form_time_label),
                required = true,
                modifier = Modifier.weight(1f),
                onClick = onTimeClick,
                readOnly = true
            )
        }

        SearchBar(
            value = location,
            onValueChange = onLocationChange,
            placeholder = stringResource(R.string.event_form_search_place),
            modifier = Modifier.fillMaxWidth()
        )

        PlaceSearchBar(
            query = location,
            onPlaceSelected = onPlaceSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.event_form_type),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )
            Switch(
                checked = isPrivate,
                onCheckedChange = onPrivateChange,
                label = stringResource(R.string.event_form_make_private)
            )
        }

        Button(
            text = if (isLoading) stringResource(R.string.create_event_submitting) else stringResource(R.string.create_event_submit),
            onClick = onCreateEvent,
            enabled = isFormValid,
            leftIcon = if (isLoading) null else Icons.Default.Add
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEventScreenPreview() {
    CreateEventContent(
        title = "Randonnée amateure",
        onTitleChange = {},
        description = "Une superbe randonnée !",
        onDescriptionChange = {},
        date = "12/12/2024",
        onDateClick = {},
        time = "14:00",
        onTimeClick = {},
        location = "Amiens",
        onLocationChange = {},
        onPlaceSelected = { _ -> },
        isPrivate = false,
        onPrivateChange = {},
        imageUri = null,
        onImageSelected = {},
        isLoading = false,
        isFormValid = true,
        onCreateEvent = {}
    )
}
