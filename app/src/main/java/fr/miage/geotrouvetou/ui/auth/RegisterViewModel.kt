package fr.miage.geotrouvetou.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.domain.models.User
import fr.miage.geotrouvetou.utils.PasswordValidation
import fr.miage.geotrouvetou.utils.UserFieldValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToMain: Boolean = false,
    val termsAccepted: Boolean = false,
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authService get() = getApplication<App>().authService
    private val databaseService get() = getApplication<App>().databaseService

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String, lastName: String, firstName: String) {
        val error = validateEmail(email) ?: validatePassword(password)
            ?: validateConfirmPassword(password, confirmPassword)
        if (error != null) {
            _uiState.value = _uiState.value.copy(error = error)
            return
        }
        if (!_uiState.value.termsAccepted) {
            _uiState.value = _uiState.value.copy(error = "Vous devez accepter les conditions d'utilisation")
            return
        }

        val fullName = "$firstName $lastName".trim()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = try {
                authService.signUp(email, password, fullName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AuthErrorTranslator.translate(e.message))
                return@launch
            }

            try {
                if (userId != null) {
                    databaseService.createProfile(User(id = userId, email = email, fullName = fullName))
                }
            } catch (_: Exception) { }

            _uiState.value = _uiState.value.copy(isLoading = false, navigateToMain = true)
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToMain = false)
    }

    fun onTermsAcceptedChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(termsAccepted = value)
    }

    fun validateEmail(email: String): String? = UserFieldValidator.validateEmail(email)

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Le mot de passe est requis"
        return PasswordValidation.of(password).firstError()
    }

    fun validateConfirmPassword(password: String, confirm: String): String? =
        PasswordValidation.confirmError(password, confirm)
}
