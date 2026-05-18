package fr.miage.geoevent.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fr.miage.geoevent.GeoEventApplication
import fr.miage.geoevent.databinding.ActivityProfileBinding
import fr.miage.geoevent.ui.auth.LoginActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    // Initialise la vue, affiche l'email de l'utilisateur connecté et gère la déconnexion.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val supabase = (applicationContext as GeoEventApplication).supabase

        // On observe le Flow sessionStatus plutôt que currentSessionOrNull() car
        // la session peut ne pas encore être propagée de façon synchrone au moment
        // où onCreate s'exécute.
        lifecycleScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    binding.tvEmail.text = status.session.user?.email ?: ""
                }
            }
        }

        binding.btnDeconnexion.setOnClickListener {
            lifecycleScope.launch {
                supabase.auth.signOut()
                Toast.makeText(this@ProfileActivity, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                // FLAG_CLEAR_TASK vide la back stack : impossible de revenir à la carte
                // sans se reconnecter.
                startActivity(
                    Intent(this@ProfileActivity, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }
}
