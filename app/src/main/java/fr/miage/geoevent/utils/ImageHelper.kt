package fr.miage.geoevent.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream

class ImageHelper(private val client: SupabaseClient) {
    private val bucketName = "EventImages"

    /**
     * Télécharge une image dans le bucket Supabase et retourne son URL publique.
     * Convertit l'image en WebP pour optimiser l'espace de stockage.
     * @param fileName Nom du fichier sans extension
     * @param bytes Contenu original de l'image (JPG/PNG)
     * @return L'URL publique de l'image stockée en .webp
     */
    suspend fun uploadEventImage(fileName: String, bytes: ByteArray): String {
        val webpBytes = convertToWebp(bytes)
        val finalFileName = if (fileName.endsWith(".webp")) fileName else "$fileName.webp"
        
        val bucket = client.storage.from(bucketName)
        bucket.upload(finalFileName, webpBytes)

        return bucket.publicUrl(finalFileName)
    }

    /**
     * Compresse les bytes d'une image au format WebP (qualité 80%).
     */
    private fun convertToWebp(bytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val out = ByteArrayOutputStream()
        
        // Utilisation du format WEBP_LOSSY (disponible depuis Android 10/API 29+)
        // Ou WEBP pour les versions plus anciennes. 
        // Ici on utilise WEBP_LOSSY à 80% de qualité pour un excellent ratio poids/qualité.
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
        
        return out.toByteArray()
    }
}
