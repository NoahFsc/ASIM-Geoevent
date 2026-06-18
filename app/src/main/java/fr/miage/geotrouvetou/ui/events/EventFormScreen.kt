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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.utils.appViewModelFactory
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ImageUploader
import fr.miage.geotrouvetou.ui.components.atoms.Input
import fr.miage.geotrouvetou.ui.components.atoms.Switch
import fr.miage.geotrouvetou.ui.components.atoms.TextArea
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.molecules.PlaceSearchBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    onSaved: () -> Unit,
    event: Evenement? = null,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel: EventFormViewModel = viewModel(
        key = event?.id,
        factory = appViewModelFactory(context),
    )

    LaunchedEffect(event) {
        viewModel.load(event)
    }

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
        viewModel.saved.collect {
            onSaved()
            onBack?.invoke()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { errorMessage = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable { onBack() },
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
                    text = stringResource(R.string.action_back),
                    fontSize = 18.sp,
                    color = colorResource(R.color.text_light)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = if (onBack == null) 32.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(
                    if (viewModel.isEditMode) R.string.update_event_title else R.string.create_event_title
                ),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.primary_600),
                lineHeight = 34.sp
            )

            ImageUploader(
                imageUri = viewModel.imageUri,
                onImageSelected = { viewModel.imageUri = it },
                label = stringResource(R.string.event_form_cover_image),
                required = true,
                imageUrl = viewModel.currentImageUrl,
            )

            Input(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                placeholder = stringResource(R.string.event_form_title_placeholder),
                label = stringResource(R.string.event_form_title_label),
                required = true,
            )

            TextArea(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
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
                    value = viewModel.date,
                    onValueChange = {},
                    placeholder = stringResource(R.string.event_form_date_placeholder),
                    label = stringResource(R.string.event_form_date_label),
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true },
                    readOnly = true
                )
                Input(
                    value = viewModel.time,
                    onValueChange = {},
                    placeholder = stringResource(R.string.event_form_time_placeholder),
                    label = stringResource(R.string.event_form_time_label),
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showTimePicker = true },
                    readOnly = true
                )
            }

            Input(
                value = viewModel.location,
                onValueChange = { viewModel.location = it },
                placeholder = stringResource(R.string.event_form_search_place),
                label = stringResource(R.string.event_form_location_label),
                required = true,
                leadingIcon = Icons.Default.Search
            )

            PlaceSearchBar(
                query = viewModel.location,
                onPlaceSelected = { place ->
                    viewModel.latitude = place.latitude
                    viewModel.longitude = place.longitude
                    viewModel.location = place.shortAddress
                },
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
                    checked = viewModel.isPrivate,
                    onCheckedChange = { viewModel.isPrivate = it },
                    label = stringResource(R.string.event_form_make_private)
                )
            }

            Button(
                text = when {
                    viewModel.isLoading && viewModel.isEditMode -> stringResource(R.string.update_event_submitting)
                    viewModel.isLoading -> stringResource(R.string.create_event_submitting)
                    viewModel.isEditMode -> stringResource(R.string.update_event_submit)
                    else -> stringResource(R.string.create_event_submit)
                },
                onClick = {
                    val bytes = viewModel.imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    viewModel.save(bytes)
                },
                enabled = viewModel.isFormValid,
                leftIcon = when {
                    viewModel.isLoading -> null
                    viewModel.isEditMode -> Icons.Default.Check
                    else -> Icons.Default.Add
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
