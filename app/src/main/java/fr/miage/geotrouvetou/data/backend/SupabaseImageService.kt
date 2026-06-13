package fr.miage.geotrouvetou.data.backend

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import fr.miage.geotrouvetou.domain.interfaces.IImageService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.ByteArrayOutputStream
import kotlin.time.Duration.Companion.hours

/** Implémentation du stockage d'images via Supabase Storage. Convertit en WebP avant upload. */
class SupabaseImageService(private val client: SupabaseClient) : IImageService {

    private val bucketName = "EventImages"
    private val bucketNameAvatar = "avatars"

    override suspend fun uploadAvatarImage(userId: String, bytes: ByteArray): String {
        val path = "avatar_$userId.webp"
        val payload = try { convertToWebp(bytes) } catch (_: Exception) { bytes }
        client.storage.from(bucketNameAvatar).upload(path, payload) { upsert = true }
        return path
    }

    override suspend fun getAvatarSignedUrl(path: String): String {
        return client.storage.from(bucketNameAvatar).createSignedUrl(path, 1.hours)
    }

    override suspend fun uploadEventImage(fileName: String, bytes: ByteArray): String {
        val finalFileName = if (fileName.endsWith(".webp")) fileName else "$fileName.webp"
        val payload = try {
            convertToWebp(bytes)
        } catch (e: Exception) {
            Log.e("SupabaseImageService", "Conversion WebP échouée, upload de l'image originale.", e)
            bytes
        }

        val bucket = client.storage.from(bucketName)
        bucket.upload(finalFileName, payload) {
            contentType = ContentType.parse("image/webp")
            upsert = true
        }
        return bucket.publicUrl(finalFileName)
    }

    /** Compresse l'image au format WebP (qualité 80 %), bon ratio poids/qualité. */
    private fun convertToWebp(bytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalArgumentException("Impossible de décoder l'image en bitmap.")

        return ByteArrayOutputStream().use { out ->
            val success = bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
            if (!success) throw IllegalStateException("Échec de la compression WebP.")
            out.toByteArray()
        }
    }
}
