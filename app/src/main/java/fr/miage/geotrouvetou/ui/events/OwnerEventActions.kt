package fr.miage.geotrouvetou.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ButtonVariant

@Composable
fun OwnerEventActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            text = stringResource(R.string.event_detail_edit),
            onClick = onEdit,
            leftIcon = Icons.Default.Edit,
            fullWidth = false,
            modifier = Modifier.weight(1f),
        )
        Button(
            text = stringResource(R.string.event_detail_delete),
            onClick = onDelete,
            variant = ButtonVariant.GhostDanger,
            leftIcon = Icons.Default.Delete,
            fullWidth = false,
            modifier = Modifier.weight(1f),
        )
    }
}
