package fr.miage.geoevent.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    required: Boolean = false,
    isPassword: Boolean = false,
    enabled: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusRequester.requestFocus()
            }
        ) {
            Text(
                text = label,
                color = Color(0xFF1F2937),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (required) {
                Text(
                    text = " *",
                    color = Color(0xFFE74C3C),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF9CA3AF),
                    fontSize = 17.sp
                )
            },
            textStyle = TextStyle(
                color = Color(0xFF1F2937),
                fontSize = 18.sp
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            trailingIcon = {
                if (isPassword) {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Masquer le mot de passe"
                            } else {
                                "Afficher le mot de passe"
                            },
                            tint = Color(0xFF4B5563)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4FA088),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                disabledBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF3F4F6),
                cursorColor = Color(0xFF4FA088)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppTextFieldEmailPreview() {
    AppTextField(
        value = "sam.archebien@gmail.com",
        onValueChange = {},
        label = "Adresse email",
        placeholder = "Entrez votre adresse email",
        required = true
    )
}

@Preview(showBackground = true)
@Composable
fun AppTextFieldPasswordPreview() {
    AppTextField(
        value = "motdepasse123",
        onValueChange = {},
        label = "Mot de passe",
        placeholder = "Entrez votre mot de passe",
        required = true,
        isPassword = true
    )
}