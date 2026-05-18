package fr.miage.geoevent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fr.miage.geoevent.GeoEventApplication
import fr.miage.geoevent.R
import fr.miage.geoevent.databinding.ActivityRegisterBinding
import fr.miage.geoevent.ui.map.MainActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // Initialise la vue et branche les listeners d'inscription et de retour.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val supabase = (applicationContext as GeoEventApplication).supabase

        binding.btnInscription.setOnClickListener {
            // "emailInput" / "passwordInput" : nommage explicite pour éviter le shadowing
            // du receiver Kotlin dans le lambda signUpWith { email = ..., password = ... }.
            val emailInput = binding.etEmail.text?.toString()?.trim().orEmpty()
            val passwordInput = binding.etPassword.text?.toString().orEmpty()
            val confirmInput = binding.etConfirmPassword.text?.toString().orEmpty()

            if (!validerChamps(emailInput, passwordInput, confirmInput)) return@setOnClickListener

            lifecycleScope.launch {
                setLoading(true)
                try {
                    supabase.auth.signUpWith(Email) {
                        email = emailInput
                        password = passwordInput
                    }
                    // La confirmation email est désactivée côté Supabase :
                    // signUpWith établit directement la session, on navigue sans vérification.
                    goToMain()
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, traduireErreur(e.message), Toast.LENGTH_LONG).show()
                    setLoading(false)
                }
            }
        }

        binding.btnRetourConnexion.setOnClickListener {
            finish()
        }
    }

    // Vérifie le format de l'email, la longueur minimale du mot de passe (6 car.)
    // et la correspondance des deux saisies de mot de passe.
    private fun validerChamps(emailInput: String, passwordInput: String, confirmInput: String): Boolean {
        var valid = true

        if (emailInput.isBlank()) {
            binding.tilEmail.error = getString(R.string.erreur_email_requis)
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            binding.tilEmail.error = getString(R.string.erreur_email_format)
            valid = false
        } else {
            binding.tilEmail.error = null
        }

        if (passwordInput.isBlank()) {
            binding.tilPassword.error = getString(R.string.erreur_mot_de_passe_requis)
            valid = false
        } else if (passwordInput.length < 6) {
            // Supabase impose un minimum de 6 caractères côté serveur,
            // on le vérifie aussi côté client pour un retour immédiat.
            binding.tilPassword.error = getString(R.string.erreur_mot_de_passe_trop_court)
            valid = false
        } else {
            binding.tilPassword.error = null
        }

        if (passwordInput.isNotBlank() && confirmInput != passwordInput) {
            binding.tilConfirmPassword.error = getString(R.string.erreur_mots_de_passe_differents)
            valid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return valid
    }

    // Les erreurs Supabase arrivent en anglais depuis l'API GoTrue.
    // On les traduit ici pour ne jamais exposer de message technique à l'utilisateur.
    private fun traduireErreur(message: String?): String = when {
        message == null -> "Une erreur inattendue s'est produite"
        message.contains("User already registered", ignoreCase = true) ->
            "Un compte existe déjà avec cette adresse email"
        message.contains("Password should be at least", ignoreCase = true) ->
            "Le mot de passe doit contenir au moins 6 caractères"
        message.contains("Unable to validate email", ignoreCase = true) ||
        message.contains("invalid email", ignoreCase = true) ->
            "Adresse email invalide"
        message.contains("rate limit", ignoreCase = true) ||
        message.contains("too many requests", ignoreCase = true) ->
            "Trop de tentatives. Réessayez dans quelques minutes."
        message.contains("network", ignoreCase = true) ||
        message.contains("Unable to connect", ignoreCase = true) ->
            "Erreur de connexion réseau. Vérifiez votre connexion internet."
        else -> "Une erreur est survenue. Réessayez."
    }

    // Affiche ou masque le ProgressBar et désactive les boutons pendant un appel réseau.
    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnInscription.isEnabled = !loading
        binding.btnRetourConnexion.isEnabled = !loading
    }

    // Navigue vers la carte en vidant la back stack : l'utilisateur ne peut pas
    // revenir à l'inscription avec le bouton retour une fois connecté.
    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
