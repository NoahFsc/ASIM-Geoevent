package fr.miage.geotrouvetou.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.utils.UserFieldValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToMain: Boolean = false,
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authService get() = getApplication<App>().authService

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        if (authService.isLoggedIn()) {
            _uiState.value = LoginUiState(navigateToMain = true)
        }
    }

    fun login(email: String, password: String) {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        if (emailError != null || passwordError != null) {
            _uiState.value = _uiState.value.copy(error = emailError ?: passwordError)
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                authService.signIn(email, password)
                _uiState.value = LoginUiState(navigateToMain = true)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = AuthErrorTranslator.translate(e.message))
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToMain = false)
    }

    fun validateEmail(email: String): String? = UserFieldValidator.validateEmail(email)

    fun validatePassword(password: String): String? =
        if (password.isBlank()) "Le mot de passe est requis" else null
}
