package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import fr.miage.geotrouvetou.R

@Composable
fun MarkerIcon(
    size: Dp,
    borderWidth: Dp,
    isPrivate: Boolean = false,
) {
    MaterialTheme {
        val bgColor = colorResource(id = if (isPrivate) R.color.warning_400 else R.color.primary_400)
        val flagColor = colorResource(id = R.color.white)

        Box(
            modifier = Modifier
                .size(size)
                .background(color = bgColor, shape = CircleShape)
                .border(width = borderWidth, color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = flagColor,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}

