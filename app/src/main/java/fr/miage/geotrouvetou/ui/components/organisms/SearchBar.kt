package fr.miage.geotrouvetou.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

private val SearchBarShape = RoundedCornerShape(50)

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = onClick != null,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = SearchBarShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                )
                .background(Color.White, SearchBarShape)
                .border(1.dp, colorResource(R.color.text_disabled), SearchBarShape)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = colorResource(R.color.text_darker),
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = colorResource(R.color.text_placeholder),
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                }
            }
        )
        // Surcouche transparente pour rendre toute la pilule cliquable en mode bouton
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            )
        }
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SearchBar(value = "", onValueChange = {}, placeholder = "Choisir votre lieu de randonnée")
    }
}
