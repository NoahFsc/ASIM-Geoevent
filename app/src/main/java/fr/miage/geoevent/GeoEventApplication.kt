package fr.miage.geoevent

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import fr.miage.geoevent.data.supabase as globalSupabase

// Point d'entrée unique pour le client Supabase : toutes les Activity y accèdent via
// (applicationContext as GeoEventApplication).supabase, sans dépendance entre elles.
class GeoEventApplication : Application() {

    // On utilise l'instance unique définie dans SupabaseClient.kt pour garantir
    // que la session Auth et les données sont partagées entre tous les composants.
    val supabase: SupabaseClient get() = globalSupabase
}
