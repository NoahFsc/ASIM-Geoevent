package fr.miage.geotrouvetou.ui.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.domain.models.AdminStats
import fr.miage.geotrouvetou.domain.models.AuditLogEntry
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val stats: AdminStats = AdminStats(),
    val users: List<User> = emptyList(),
    val events: List<Evenement> = emptyList(),
    val recentActivity: List<AuditLogEntry> = emptyList(),
    val currentUserId: String = "",
    val isLoadingStats: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isLoadingEvents: Boolean = false,
    val isLoadingActivity: Boolean = false,
    val hasMoreUsers: Boolean = true,
    val hasMoreEvents: Boolean = true,
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val authService get() = getApplication<App>().authService
    private val databaseService get() = getApplication<App>().databaseService

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var usersPage = 0
    private var eventsPage = 0

    init {
        _uiState.value = _uiState.value.copy(currentUserId = authService.currentUserId() ?: "")
        loadStats()
        loadRecentActivity()
        loadUsers()
        loadEvents()
    }

    // ── Stats ──

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true)
            try {
                val stats = databaseService.getAdminStats()
                _uiState.value = _uiState.value.copy(stats = stats, isLoadingStats = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingStats = false)
            }
        }
    }

    // ── Activité récente ──

    fun loadRecentActivity() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingActivity = true)
            try {
                val activity = databaseService.getRecentActivity(limit = 10)
                _uiState.value = _uiState.value.copy(recentActivity = activity, isLoadingActivity = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingActivity = false)
            }
        }
    }

    // ── Utilisateurs ──

    fun loadUsers() {
        usersPage = 0
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUsers = true, users = emptyList(), hasMoreUsers = true)
            try {
                val page = databaseService.getAdminUsers(page = 0, pageSize = PAGE_SIZE)
                _uiState.value = _uiState.value.copy(users = page, isLoadingUsers = false, hasMoreUsers = page.size == PAGE_SIZE)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingUsers = false)
            }
        }
    }

    fun loadMoreUsers() {
        if (!_uiState.value.hasMoreUsers || _uiState.value.isLoadingUsers) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUsers = true)
            try {
                usersPage++
                val page = databaseService.getAdminUsers(page = usersPage, pageSize = PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users + page,
                    isLoadingUsers = false,
                    hasMoreUsers = page.size == PAGE_SIZE,
                )
            } catch (_: Exception) {
                usersPage--
                _uiState.value = _uiState.value.copy(isLoadingUsers = false)
            }
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                databaseService.updateUserRole(userId, newRole)
                _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users.map { if (it.id == userId) it.copy(role = newRole) else it },
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Échec mise à jour rôle", e)
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                databaseService.adminDeleteUser(userId)
                _uiState.value = _uiState.value.copy(
                    users = _uiState.value.users.filter { it.id != userId },
                    stats = _uiState.value.stats.copy(userCount = (_uiState.value.stats.userCount - 1).coerceAtLeast(0)),
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Échec suppression utilisateur", e)
            }
        }
    }

    // ── Événements ──

    fun loadEvents() {
        eventsPage = 0
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvents = true, events = emptyList(), hasMoreEvents = true)
            try {
                val page = databaseService.getAdminEvents(page = 0, pageSize = PAGE_SIZE)
                _uiState.value = _uiState.value.copy(events = page, isLoadingEvents = false, hasMoreEvents = page.size == PAGE_SIZE)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingEvents = false)
            }
        }
    }

    fun loadMoreEvents() {
        if (!_uiState.value.hasMoreEvents || _uiState.value.isLoadingEvents) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvents = true)
            try {
                eventsPage++
                val page = databaseService.getAdminEvents(page = eventsPage, pageSize = PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events + page,
                    isLoadingEvents = false,
                    hasMoreEvents = page.size == PAGE_SIZE,
                )
            } catch (_: Exception) {
                eventsPage--
                _uiState.value = _uiState.value.copy(isLoadingEvents = false)
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                databaseService.deleteEvent(eventId)
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events.filter { it.id != eventId },
                    stats = _uiState.value.stats.copy(eventCount = (_uiState.value.stats.eventCount - 1).coerceAtLeast(0)),
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Échec suppression événement", e)
            }
        }
    }
}
