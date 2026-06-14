package fr.miage.geotrouvetou.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

@Composable
fun EventDetailCard(
    date: String,
    time: String,
    locationName: String,
    locationDetail: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorResource(R.color.text_disabled), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.white))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailRow(
            icon = Icons.Outlined.CalendarMonth,
            title = date,
            subtitle = time,
        )
        HorizontalDivider(color = colorResource(R.color.text_disabled))
        DetailRow(
            icon = Icons.Outlined.LocationOn,
            title = locationName,
            subtitle = locationDetail,
        )
    }
}

@Composable
private fun DetailRow(icon: ImageVector, title: String, subtitle: String?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.primary_transparent)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(R.color.primary_600),
                modifier = Modifier.size(26.dp),
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_lighter),
                )
            }
        }
    }
}

@Preview(name = "EventDetailCard")
@Composable
private fun EventDetailCardPreview() {
    Box(modifier = Modifier.background(colorResource(R.color.text_darker)).padding(16.dp)) {
        EventDetailCard(
            date = "Samedi, 28 Avril",
            time = "09:00",
            locationName = "Forêt de Chailluz",
            locationDetail = "Besançon, France",
        )
    }
}
