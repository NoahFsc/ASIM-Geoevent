package fr.miage.geotrouvetou.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ButtonVariant
import fr.miage.geotrouvetou.ui.components.atoms.Input
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.utils.PasswordValidation
import kotlinx.coroutines.launch

@Composable
fun EditPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: EditPasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saveToastKey by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onBackClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colorResource(R.color.text_darker),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.action_back),
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_darker),
                )
            }

            Text(
                text = stringResource(R.string.edit_password_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
                lineHeight = 38.sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.password_rules_title_new),
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_lighter),
                )
                PasswordRequirement(label = stringResource(R.string.password_rule_min), met = uiState.validation.hasMinLength)
                PasswordRequirement(label = stringResource(R.string.password_rule_digit), met = uiState.validation.hasDigit)
                PasswordRequirement(label = stringResource(R.string.password_rule_special), met = uiState.validation.hasSpecial)
                PasswordRequirement(label = stringResource(R.string.password_rule_uppercase), met = uiState.validation.hasUppercase)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Input(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "••••••••••••",
                    label = stringResource(R.string.edit_password_new_label),
                    required = true,
                    visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = if (uiState.showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    onTrailingIconClick = viewModel::toggleShowPassword,
                )
                Input(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "••••••••••••",
                    label = stringResource(R.string.edit_password_confirm_label),
                    required = true,
                    visualTransformation = if (uiState.showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = if (uiState.showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    onTrailingIconClick = viewModel::toggleShowConfirmPassword,
                    error = PasswordValidation.confirmError(context, uiState.password, uiState.confirmPassword),
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = colorResource(R.color.danger_500),
                    fontSize = 14.sp,
                )
            }
        }

        Button(
            text = stringResource(R.string.edit_password_submit),
            onClick = {
                scope.launch {
                    val success = viewModel.savePassword()
                    if (success) saveToastKey++
                }
            },
            variant = ButtonVariant.Fill,
            enabled = uiState.formValid && !uiState.isSaving,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        )
    }

    if (saveToastKey > 0) {
        Toast(
            title = stringResource(R.string.edit_password_toast_title),
            description = stringResource(R.string.edit_password_toast_desc),
            key = saveToastKey,
        )
    }
}

@Composable
private fun PasswordRequirement(label: String, met: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (met) colorResource(R.color.success_500) else colorResource(R.color.danger_500),
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = colorResource(R.color.text_darker),
        )
    }
}
