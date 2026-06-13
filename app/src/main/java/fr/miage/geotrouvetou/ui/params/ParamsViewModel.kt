package fr.miage.geotrouvetou.ui.params

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParamsUiState(
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,
    val isSigningOut: Boolean = false,
    val error: String? = null,
    val navigateToLogin: Boolean = false,
)

class ParamViewModel(application: Application) : AndroidViewModel(application) {

    private val authService get() = getApplication<App>().authService
    private val databaseService get() = getApplication<App>().databaseService

    private val _uiState = MutableStateFlow(ParamsUiState())
    val uiState: StateFlow<ParamsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authService.currentUserId() ?: return@launch
            val profile = runCatching { databaseService.getProfile(userId) }.getOrNull()
            _uiState.value = _uiState.value.copy(isAdmin = profile?.role == "admin")
        }
    }

    suspend fun signOut(): Boolean {
        _uiState.value = _uiState.value.copy(isLoading = true)
        return try {
            authService.signOut()
            true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Erreur lors de la déconnexion")
            false
        }
    }
}
