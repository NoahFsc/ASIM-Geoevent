package fr.miage.geotrouvetou.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.auth.modals.PasswordInfoModal
import fr.miage.geotrouvetou.utils.PasswordValidation
import fr.miage.geotrouvetou.utils.UserFieldValidator
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.Checkbox
import fr.miage.geotrouvetou.ui.components.atoms.Input

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var showPasswordInfo by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateToMain) {
        if (uiState.navigateToMain) {
            onRegisterSuccess()
            viewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.register_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_darker),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                text = stringResource(R.string.register_have_account),
                fontSize = 15.sp,
                color = colorResource(R.color.text_lighter),
            )
            Text(
                text = stringResource(R.string.register_login_link),
                fontSize = 15.sp,
                color = colorResource(R.color.primary_500),
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onBackClick() },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Input(
                    value = lastName,
                    onValueChange = { lastName = UserFieldValidator.capitalizeFirst(it) },
                    placeholder = stringResource(R.string.field_lastname_placeholder),
                    label = stringResource(R.string.field_lastname_label),
                    required = true,
                    modifier = Modifier.weight(1f),
                )
                Input(
                    value = firstName,
                    onValueChange = { firstName = UserFieldValidator.capitalizeFirst(it) },
                    placeholder = stringResource(R.string.field_firstname_placeholder),
                    label = stringResource(R.string.field_firstname_label),
                    required = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Input(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.field_email_placeholder),
                label = stringResource(R.string.field_email_label),
                required = true,
            )
            Input(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.field_password_placeholder),
                label = stringResource(R.string.field_password_label),
                required = true,
                trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                labelTrailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.password_info_cd),
                        tint = colorResource(R.color.text_lighter),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showPasswordInfo = true },
                    )
                },
            )

            Input(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(R.string.register_confirm_placeholder),
                label = stringResource(R.string.register_confirm_label),
                required = true,
                trailingIcon = if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                onTrailingIconClick = { confirmVisible = !confirmVisible },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                error = PasswordValidation.confirmError(context, password, confirmPassword),
            )
            Checkbox(
                checked = uiState.termsAccepted,
                onCheckedChange = { viewModel.onTermsAcceptedChange(it) },
            ) {
                val danger = colorResource(R.color.danger_500)
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.register_terms_prefix))
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = colorResource(R.color.primary_600))) {
                            append(stringResource(R.string.register_terms_link))
                        }
                        withStyle(SpanStyle(color = danger)) {
                            append("*")
                        }
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_darker),
                )
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.error!!,
                color = colorResource(R.color.danger_500),
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        val formValid = UserFieldValidator.isLastNameValid(lastName) && UserFieldValidator.isFirstNameValid(firstName) &&
            UserFieldValidator.isEmailValid(email) && password.isNotBlank() &&
            PasswordValidation.passwordsMatch(password, confirmPassword) && uiState.termsAccepted

        if (uiState.isLoading) {
            CircularProgressIndicator(color = colorResource(R.color.primary_500))
        } else {
            Button(
                text = stringResource(R.string.register_submit),
                onClick = { viewModel.register(email, password, confirmPassword, lastName, firstName) },
                fullWidth = true,
                enabled = formValid,
            )
        }
    }

    if (showPasswordInfo) {
        PasswordInfoModal(onDismiss = { showPasswordInfo = false })
    }
}
