package fr.miage.geotrouvetou.ui.events

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import fr.miage.geotrouvetou.R

/** Confirmation de suppression d'un événement, partagée entre la page détail et la modale. */
@Composable
fun DeleteEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.white),
        title = { Text(stringResource(R.string.delete_event_title)) },
        text = { Text(stringResource(R.string.delete_event_desc)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete_event_confirm),
                    color = colorResource(R.color.danger_500),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
