package fr.miage.geoevent.data

import fr.miage.geoevent.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

// Instance globale du client Supabase. 
// Centraliser cette instance ici permet de partager la même session d'authentification 
// entre les activités (Login/Register) et les ViewModels (MainViewModel).
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Auth)      // Nécessaire pour gérer l'utilisateur connecté (ID, session)
    install(Postgrest) // Pour les opérations CRUD sur la base de données
    install(Realtime)  // Pour la mise à jour automatique de la carte
    install(Storage)   // Pour gérer l'upload des photos dans le bucket EventImages
}
