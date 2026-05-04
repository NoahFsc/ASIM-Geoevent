package fr.miage.geoevent

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fr.miage.geoevent.databinding.ActivityMainBinding
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable
import fr.miage.geoevent.BuildConfig
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
}

@Serializable
data class TestItem(
    val id: Long,
    val created_at: String,
    val name: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. On peut utiliser "supabase" ici pour voir le contenu
        lifecycleScope.launch {
            try {
                // On récupère les données de Test_Table
                val items = supabase.from("Test_Table").select().decodeList<TestItem>()

                // On affiche le résultat
                Log.d("SupabaseTest", "Succès ! Liste complète : $items")

                if (items.isNotEmpty()) {
                    Log.d("SupabaseTest", "La valeur de test est : ${items[0].name}")
                }

            } catch (e: Exception) {
                Log.e("SupabaseTest", "Erreur lors de la récupération : ${e.message}")
            }
        }
    }
}