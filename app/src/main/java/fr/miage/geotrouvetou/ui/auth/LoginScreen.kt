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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.Input

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.navigateToMain) {
        if (uiState.navigateToMain) {
            onLoginSuccess()
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
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = stringResource(R.string.login_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_darker),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                text = stringResource(R.string.login_no_account),
                fontSize = 16.sp,
                color = colorResource(R.color.text_lighter),
            )
            Text(
                text = stringResource(R.string.login_signup_link),
                fontSize = 16.sp,
                color = colorResource(R.color.primary_500),
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRegisterClick() },
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
            )
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.error!!,
                color = colorResource(R.color.danger_500),
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = colorResource(R.color.primary_500))
        } else {
            Button(
                text = stringResource(R.string.login_submit),
                onClick = { viewModel.login(email, password) },
                fullWidth = true,
                enabled = email.isNotBlank() && password.isNotBlank(),
            )
        }
    }
}
