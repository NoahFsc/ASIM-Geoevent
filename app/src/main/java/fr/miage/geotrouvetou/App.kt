package fr.miage.geotrouvetou

import android.app.Application
import android.content.Context
import fr.miage.geotrouvetou.data.backend.SupabaseAuthService
import fr.miage.geotrouvetou.data.backend.SupabaseDatabaseService
import fr.miage.geotrouvetou.data.backend.SupabaseImageService
import fr.miage.geotrouvetou.data.geocoding.NominatimGeocodingService
import fr.miage.geotrouvetou.data.maps.OSMMapService
import fr.miage.geotrouvetou.domain.interfaces.IAuthService
import fr.miage.geotrouvetou.domain.interfaces.IDatabaseService
import fr.miage.geotrouvetou.domain.interfaces.IGeocodingService
import fr.miage.geotrouvetou.domain.interfaces.IImageService
import fr.miage.geotrouvetou.domain.interfaces.IMapService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Point d'entrée de l'app et ServiceLocator : expose les services derrière leurs interfaces.
 * Changer de backend ne nécessite que de remplacer les implémentations instanciées ici.
 */
class App : Application() {

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            httpEngine = OkHttp.create()
            install(Auth)      // utilisateur connecté (id, session)
            install(Postgrest) // CRUD base de données
            install(Realtime)  // mise à jour automatique de la carte
            install(Storage)   // upload des images
        }
    }

    val authService: IAuthService by lazy { SupabaseAuthService(supabase) }
    val imageService: IImageService by lazy { SupabaseImageService(supabase) }
    val databaseService: IDatabaseService by lazy { SupabaseDatabaseService(supabase, imageService) }
    val geocodingService: IGeocodingService by lazy { NominatimGeocodingService() }

    /** La carte dépend d'un Context d'écran, donc créée à la demande plutôt qu'en singleton. */
    fun createMapService(context: Context): IMapService = OSMMapService(context)
}
