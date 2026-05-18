package fr.miage.geoevent

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

// Point d'entrée unique pour le client Supabase : toutes les Activity y accèdent via
// (applicationContext as GeoEventApplication).supabase, sans dépendance entre elles.
class GeoEventApplication : Application() {

    // "by lazy" garantit une seule instance créée à la première utilisation,
    // jamais avant que BuildConfig soit disponible.
    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
