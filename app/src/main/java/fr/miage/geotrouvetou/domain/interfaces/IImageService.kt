package fr.miage.geotrouvetou.domain.interfaces

interface IImageService {
    suspend fun uploadEventImage(fileName: String, bytes: ByteArray): String
    suspend fun uploadAvatarImage(userId: String, bytes: ByteArray): String
    suspend fun getAvatarSignedUrl(path: String): String
}
