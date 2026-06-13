package fr.miage.geotrouvetou.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement

@Composable
fun AdminEventCard(
    event: Evenement,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = event.image_url,
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(R.color.text_disabled)),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = event.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )
            Box(
                modifier = Modifier
                    .background(
                        colorResource(if (event.visibility) R.color.success_400 else R.color.warning_400),
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = if (event.visibility) stringResource(R.string.admin_event_public) else stringResource(R.string.admin_event_private),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_darker),
                )
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.admin_menu_cd),
                    tint = colorResource(R.color.text_lighter),
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Modifier", fontSize = 14.sp, color = colorResource(R.color.text_disabled)) },
                    onClick = { menuExpanded = false },
                    enabled = false,
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Supprimer", fontSize = 14.sp, color = colorResource(R.color.danger_500)) },
                    onClick = { menuExpanded = false; onDelete() },
                )
            }
        }
    }
}

@Preview(name = "AdminEventCard – Public")
@Composable
private fun AdminEventCardPreview() {
    Box(modifier = Modifier.background(colorResource(R.color.background)).padding(16.dp)) {
        AdminEventCard(
            event = Evenement(title = "Bois de la Joux", description = "", latitude = 0.0, longitude = 0.0, visibility = true),
            onDelete = {},
        )
    }
}
