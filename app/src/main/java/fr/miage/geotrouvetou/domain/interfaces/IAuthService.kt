package fr.miage.geotrouvetou.domain.interfaces

import kotlinx.coroutines.flow.Flow

data class AuthUser(val id: String, val email: String)

interface IAuthService {
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, fullName: String): String?
    suspend fun signOut()
    suspend fun updatePassword(newPassword: String)
    fun currentUserId(): String?
    fun currentUserEmail(): String?
    fun isLoggedIn(): Boolean
    fun observeAuthenticatedUser(): Flow<AuthUser>
}
