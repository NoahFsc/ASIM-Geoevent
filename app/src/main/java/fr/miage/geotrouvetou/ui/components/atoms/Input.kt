package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    label: String? = null,
    required: Boolean = false,
    error: String? = null,
    labelTrailingContent: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(label)
                        if (required) {
                            withStyle(SpanStyle(color = colorResource(id = R.color.danger_500))) {
                                append(" *")
                            }
                        }
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_darker),
                )
                labelTrailingContent?.invoke()
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Box(modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly || onClick != null,
                enabled = onClick == null,
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = {
                    Text(text = placeholder, color = colorResource(id = R.color.text_placeholder), fontSize = 16.sp)
                },
                leadingIcon = leadingIcon?.let {
                    { Icon(imageVector = it, contentDescription = null, tint = colorResource(id = R.color.text_lighter)) }
                },
                trailingIcon = trailingIcon?.let { icon ->
                    {
                        if (onTrailingIconClick != null) {
                            IconButton(onClick = onTrailingIconClick) {
                                Icon(imageVector = icon, contentDescription = null, tint = colorResource(id = R.color.text_lighter))
                            }
                        } else {
                            Icon(imageVector = icon, contentDescription = null, tint = colorResource(id = R.color.text_lighter))
                        }
                    }
                },
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        colorResource(id = if (error != null) R.color.danger_500 else R.color.text_lighter),
                        RoundedCornerShape(8.dp),
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = colorResource(id = R.color.text_darker),
                    unfocusedTextColor = colorResource(id = R.color.text_darker),
                    disabledTextColor = colorResource(id = R.color.text_darker),
                    disabledPlaceholderColor = colorResource(id = R.color.text_placeholder),
                    disabledLeadingIconColor = colorResource(id = R.color.text_lighter),
                    disabledTrailingIconColor = colorResource(id = R.color.text_lighter),
                ),
                singleLine = true,
            )
            DropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = suggestion,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.text_dark),
                            )
                        },
                        onClick = { onSuggestionSelected(suggestion) },
                    )
                }
            }
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 16.sp,
                color = colorResource(id = R.color.danger_500),
            )
        }
    }
}

@Preview
@Composable
private fun InputPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        Input(
            value = "Fête de la musique",
            onValueChange = {},
            placeholder = "Rechercher un événement",
            label = "Titre de l'événement",
            required = true,
            leadingIcon = Icons.Default.Search,
            error = "Ce champ est requis",
        )
    }
}
