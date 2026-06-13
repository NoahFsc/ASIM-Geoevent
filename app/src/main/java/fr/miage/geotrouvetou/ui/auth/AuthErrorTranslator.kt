package fr.miage.geotrouvetou.ui.auth

/** Traduit les messages d'erreur du fournisseur d'auth en messages utilisateur en français. */
object AuthErrorTranslator {

    fun translate(message: String?): String = when {
        message == null -> "Une erreur inattendue s'est produite"
        message.contains("Invalid login credentials", ignoreCase = true) ->
            "Email ou mot de passe incorrect"
        message.contains("Email not confirmed", ignoreCase = true) ->
            "Veuillez confirmer votre email avant de vous connecter"
        message.contains("User already registered", ignoreCase = true) ->
            "Un compte existe déjà avec cette adresse email"
        message.contains("Password should be at least", ignoreCase = true) ->
            "Le mot de passe doit contenir au moins 6 caractères"
        message.contains("rate limit", ignoreCase = true) ||
            message.contains("too many requests", ignoreCase = true) ->
            "Trop de tentatives. Réessayez dans quelques minutes."
        message.contains("network", ignoreCase = true) ||
            message.contains("Unable to connect", ignoreCase = true) ->
            "Erreur de connexion réseau. Vérifiez votre connexion internet."
        else -> "Une erreur est survenue. Réessayez."
    }
}
