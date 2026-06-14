package fr.miage.geotrouvetou.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.utils.UserFieldValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val originalLastName: String = "",
    val originalFirstName: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val saveToastKey: Int = 0,
    val avatarToastKey: Int = 0,
    val error: String? = null,
    val navigateToLogout: Boolean = false,
) {
    val hasChanges: Boolean get() = lastName != originalLastName || firstName != originalFirstName
    val formValid: Boolean get() = UserFieldValidator.isLastNameValid(lastName) && UserFieldValidator.isFirstNameValid(firstName)
}

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authService get() = getApplication<App>().authService
    private val databaseService get() = getApplication<App>().databaseService

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authService.observeAuthenticatedUser().collect { user ->
                loadProfile(user.id, user.email)
            }
        }
    }

    private suspend fun loadProfile(userId: String, email: String) {
        try {
            val profile = databaseService.getProfile(userId)
            val fullName = profile?.fullName ?: ""
            val parts = fullName.split(" ", limit = 2)
            val lastName = UserFieldValidator.capitalizeFirst(parts.getOrNull(0) ?: "")
            val firstName = UserFieldValidator.capitalizeFirst(parts.getOrNull(1) ?: "")
            _uiState.value = _uiState.value.copy(
                lastName = lastName, firstName = firstName, email = email,
                originalLastName = lastName, originalFirstName = firstName,
                avatarUrl = profile?.avatarUrl,
                isLoading = false,
            )
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            lastName = UserFieldValidator.capitalizeFirst(value),
            error = null,
        )
    }

    fun onFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            firstName = UserFieldValidator.capitalizeFirst(value),
            error = null,
        )
    }

    fun save() {
        val state = _uiState.value
        if (!state.hasChanges || !state.formValid) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            try {
                val userId = authService.currentUserId() ?: return@launch
                val fullName = "${state.lastName} ${state.firstName}"
                databaseService.updateProfile(userId, fullName)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    originalLastName = state.lastName,
                    originalFirstName = state.firstName,
                    saveToastKey = _uiState.value.saveToastKey + 1,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = getApplication<App>().getString(R.string.edit_profile_save_error))
            }
        }
    }

    fun updateAvatar(bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingAvatar = true, error = null)
            try {
                val userId = authService.currentUserId() ?: return@launch
                databaseService.updateAvatar(userId, bytes)
                val profile = databaseService.getProfile(userId)
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    avatarUrl = profile?.avatarUrl,
                    avatarToastKey = _uiState.value.avatarToastKey + 1,
                )
            } catch (e: Exception) {
                Log.e("EditProfile", "updateAvatar failed", e)
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    error = getApplication<App>().getString(R.string.edit_profile_photo_error),
                )
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val userId = authService.currentUserId() ?: return@launch
                databaseService.deleteProfile(userId)
                authService.signOut()
                _uiState.value = _uiState.value.copy(navigateToLogout = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(error = getApplication<App>().getString(R.string.edit_profile_delete_error))
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToLogout = false)
    }
}
