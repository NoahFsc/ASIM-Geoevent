package fr.miage.geotrouvetou.domain.interfaces

/** Interface de stockage d'images, indépendante du fournisseur (Supabase Storage aujourd'hui). */
interface IImageService {
    suspend fun uploadEventImage(fileName: String, bytes: ByteArray): String
    suspend fun uploadAvatarImage(userId: String, bytes: ByteArray): String
    suspend fun getAvatarSignedUrl(path: String): String
}
