package fr.miage.geotrouvetou.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.StatusTag
import fr.miage.geotrouvetou.ui.components.atoms.TagStatus

@Composable
fun EventCard(
    tag: TagStatus,
    title: String,
    date: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRecommended: Boolean = false,
    attendance: String? = null,
    onDelete: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.white))
            .border(1.dp, colorResource(R.color.text_disabled), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusTag(status = tag)

            when {
                onDelete != null -> {
                    Row(
                        modifier = Modifier.clickable(onClick = onDelete),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.event_card_delete),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.text_darker),
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.event_card_delete),
                            tint = colorResource(R.color.text_darker),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                isRecommended -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colorResource(R.color.primary_500),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.event_card_recommended),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.primary_500),
                        )
                    }
                }
            }
        }

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_darker),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.event_card_datetime, date, time),
                fontSize = 14.sp,
                color = colorResource(R.color.text_lighter),
            )

            if (attendance != null && onDelete == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = colorResource(R.color.text_lighter),
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = attendance,
                        fontSize = 14.sp,
                        color = colorResource(R.color.text_lighter),
                    )
                    Text(text = "•", fontSize = 14.sp, color = colorResource(R.color.text_lighter))
                    Icon(
                        imageVector = Icons.Outlined.DirectionsWalk,
                        contentDescription = null,
                        tint = colorResource(R.color.text_lighter),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventCardPreview() {
    Box(modifier = Modifier.background(colorResource(R.color.text_darker)).padding(16.dp)) {
        EventCard(
            tag = TagStatus.NEW,
            title = "Forêt d'Ornans",
            date = "28/04",
            time = "09:00",
            isRecommended = true,
            attendance = "Faible",
            onClick = {},
        )
    }
}
