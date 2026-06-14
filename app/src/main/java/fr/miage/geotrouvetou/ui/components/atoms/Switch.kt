package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

private val TrackWidth = 60.dp
private val TrackHeight = 34.dp
private val ThumbSize = 30.dp
private val ThumbPadding = 2.dp

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val primary = colorResource(R.color.primary_600)
    val inactive = colorResource(R.color.text_disabled)

    val trackColor by animateColorAsState(
        targetValue = if (checked || isPressed) primary else inactive,
        animationSpec = tween(200),
        label = "trackColor"
    )
    val thumbOffsetX by animateDpAsState(
        targetValue = if (checked) TrackWidth - ThumbSize - ThumbPadding else ThumbPadding,
        animationSpec = tween(200),
        label = "thumbOffset"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (label != null) 16.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(TrackWidth, TrackHeight)
                .clip(RoundedCornerShape(TrackHeight / 2))
                .background(trackColor)
                .clickable(interactionSource = interactionSource, indication = null) {
                    onCheckedChange(!checked)
                }
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetX, y = ThumbPadding)
                    .size(ThumbSize)
                    .background(Color.White, CircleShape)
            )
        }
        if (label != null) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.text_darker),
            )
        }
    }
}

@Preview
@Composable
private fun SwitchPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        Switch(checked = true, onCheckedChange = {}, label = "Notifications")
    }
}
