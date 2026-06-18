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

class App : Application() {

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            httpEngine = OkHttp.create()
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    val authService: IAuthService by lazy { SupabaseAuthService(supabase) }
    val imageService: IImageService by lazy { SupabaseImageService(supabase) }
    val databaseService: IDatabaseService by lazy { SupabaseDatabaseService(supabase, imageService) }
    val geocodingService: IGeocodingService by lazy { NominatimGeocodingService() }
    fun createMapService(context: Context): IMapService = OSMMapService(context)
}
