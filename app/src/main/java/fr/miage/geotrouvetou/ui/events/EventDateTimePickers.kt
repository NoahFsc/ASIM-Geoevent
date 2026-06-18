package fr.miage.geotrouvetou.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import fr.miage.geotrouvetou.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFutureDatePickerState(): DatePickerState = rememberDatePickerState(
    selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
            utcTimeMillis >= System.currentTimeMillis() - 86_400_000
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDateTimePickers(
    showDatePicker: Boolean,
    showTimePicker: Boolean,
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    onDismissDatePicker: () -> Unit,
    onDismissTimePicker: () -> Unit,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
) {
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = onDismissDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                        onDateSelected(formatter.format(Date(millis)))
                    }
                    onDismissDatePicker()
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDatePicker) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = onDismissTimePicker) {
            Column(
                modifier = Modifier
                    .background(colorResource(R.color.white), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.event_time_picker_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker)
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissTimePicker) { Text(stringResource(R.string.action_cancel)) }
                    TextButton(onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onTimeSelected("$hour:$minute")
                        onDismissTimePicker()
                    }) { Text(stringResource(R.string.action_confirm)) }
                }
            }
        }
    }
}
