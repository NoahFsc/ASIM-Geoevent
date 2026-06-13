package fr.miage.geotrouvetou.data.backend

import fr.miage.geotrouvetou.domain.interfaces.AuthUser
import fr.miage.geotrouvetou.domain.interfaces.IAuthService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Implémentation de l'authentification via Supabase Auth. */
class SupabaseAuthService(private val client: SupabaseClient) : IAuthService {

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String, fullName: String): String? {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("full_name", fullName) }
        }
        return client.auth.currentUserOrNull()?.id
    }

    override suspend fun signOut() = client.auth.signOut()

    override suspend fun updatePassword(newPassword: String) {
        client.auth.updateUser { password = newPassword }
    }

    override fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    override fun currentUserEmail(): String? = client.auth.currentUserOrNull()?.email

    override fun isLoggedIn(): Boolean = client.auth.currentSessionOrNull() != null

    override fun observeAuthenticatedUser(): Flow<AuthUser> =
        client.auth.sessionStatus
            .filterIsInstance<SessionStatus.Authenticated>()
            .mapNotNull { status ->
                val user = status.session.user ?: return@mapNotNull null
                AuthUser(id = user.id, email = user.email ?: "")
            }
}
