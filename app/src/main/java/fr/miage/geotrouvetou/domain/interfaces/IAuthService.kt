package fr.miage.geotrouvetou.domain.interfaces

import kotlinx.coroutines.flow.Flow

/** Utilisateur authentifié, indépendant du fournisseur d'auth. */
data class AuthUser(val id: String, val email: String)

/**
 * Abstraction de l'authentification : isole le reste de l'app du fournisseur
 * (Supabase aujourd'hui). Permet de changer de backend sans toucher aux ViewModels.
 */
interface IAuthService {
    suspend fun signIn(email: String, password: String)
    /** Inscrit l'utilisateur et renvoie son id, ou null si la session n'est pas encore active. */
    suspend fun signUp(email: String, password: String, fullName: String): String?
    suspend fun signOut()
    suspend fun updatePassword(newPassword: String)
    fun currentUserId(): String?
    fun currentUserEmail(): String?
    fun isLoggedIn(): Boolean
    /** Émet à chaque fois qu'une session devient authentifiée. */
    fun observeAuthenticatedUser(): Flow<AuthUser>
}
