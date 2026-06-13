package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

enum class ButtonVariant { Fill, Ghost, GhostDark, GhostDanger, FillDanger }

private val ButtonShape = RoundedCornerShape(10.dp)

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Fill,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    leftIcon: ImageVector? = null,
    rightIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = when (variant) {
        ButtonVariant.Fill -> when {
            !enabled -> colorResource(R.color.text_light)
            isPressed -> colorResource(R.color.primary_400)
            else -> colorResource(R.color.primary_500)
        }
        ButtonVariant.FillDanger -> colorResource(R.color.danger_500)
        ButtonVariant.Ghost, ButtonVariant.GhostDark, ButtonVariant.GhostDanger ->
            if (isPressed) colorResource(R.color.primary_transparent) else Color.Transparent
    }

    val contentColor = when (variant) {
        ButtonVariant.Fill -> if (!enabled) colorResource(R.color.text_darker) else colorResource(R.color.white)
        ButtonVariant.FillDanger -> colorResource(R.color.white)
        ButtonVariant.Ghost -> when {
            !enabled -> colorResource(R.color.text_darker)
            isPressed -> colorResource(R.color.primary_400)
            else -> colorResource(R.color.primary_500)
        }
        ButtonVariant.GhostDark -> colorResource(R.color.text_darker)
        ButtonVariant.GhostDanger -> colorResource(R.color.danger_500)
    }

    val border = if (variant == ButtonVariant.GhostDanger) {
        BorderStroke(1.5.dp, colorResource(R.color.danger_500))
    } else null

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.then(if (fullWidth) Modifier.fillMaxWidth() else Modifier).height(56.dp),
        shape = ButtonShape,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (variant == ButtonVariant.Fill) colorResource(R.color.text_light) else Color.Transparent,
            disabledContentColor = if (variant == ButtonVariant.Fill) colorResource(R.color.white) else colorResource(R.color.text_darker),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        if (leftIcon != null) {
            Icon(imageVector = leftIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = if (leftIcon != null && rightIcon != null) Modifier.weight(1f) else Modifier,
        )
        if (rightIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = rightIcon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        Button(text = "Button", onClick = {}, leftIcon = Icons.Filled.Add)
    }
}
