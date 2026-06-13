package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

private val TextAreaShape = RoundedCornerShape(12.dp)

@Composable
fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    fieldHeight: Dp = 160.dp,
) {
    val borderColor = colorResource(R.color.text_lighter)
    val textColor = colorResource(R.color.text_darker)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (required) {
                        withStyle(SpanStyle(color = colorResource(R.color.danger_500))) {
                            append(" *")
                        }
                    }
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, borderColor, TextAreaShape)
                .background(Color.White, TextAreaShape)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = textColor,
                ),
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
            Text(
                text = "${value.length} / $maxLength",
                modifier = Modifier.align(Alignment.End),
                fontSize = 12.sp,
                color = colorResource(R.color.text_lighter),
            )
        }
    }
}

@Preview
@Composable
private fun TextAreaPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        TextArea(
            value = "",
            onValueChange = {},
            placeholder = "Randonnée amateure",
            maxLength = 500,
            label = "Description",
            required = true,
        )
    }
}
