package fr.miage.geotrouvetou.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.utils.PasswordValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EditPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validation: PasswordValidation = PasswordValidation.EMPTY,
) {
    val formValid: Boolean get() = validation.isValid && PasswordValidation.passwordsMatch(password, confirmPassword)
}

class EditPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val authService get() = getApplication<App>().authService

    private val _uiState = MutableStateFlow(EditPasswordUiState())
    val uiState: StateFlow<EditPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            validation = PasswordValidation.of(value),
            error = null,
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun toggleShowConfirmPassword() {
        _uiState.value = _uiState.value.copy(showConfirmPassword = !_uiState.value.showConfirmPassword)
    }

    suspend fun savePassword(): Boolean {
        val state = _uiState.value
        if (!state.formValid) return false
        _uiState.value = state.copy(isSaving = true, error = null)
        return try {
            authService.updatePassword(state.password)
            _uiState.value = _uiState.value.copy(isSaving = false)
            true
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isSaving = false, error = getApplication<App>().getString(R.string.edit_password_error))
            false
        }
    }
}
