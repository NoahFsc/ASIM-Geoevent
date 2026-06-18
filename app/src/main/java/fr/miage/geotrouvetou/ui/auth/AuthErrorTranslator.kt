package fr.miage.geotrouvetou.ui.auth

import android.content.Context
import fr.miage.geotrouvetou.R

object AuthErrorTranslator {

    fun translate(context: Context, message: String?): String {
        val res = when {
            message == null -> R.string.auth_error_unexpected
            message.contains("Invalid login credentials", ignoreCase = true) -> R.string.auth_error_invalid_credentials
            message.contains("Email not confirmed", ignoreCase = true) -> R.string.auth_error_email_not_confirmed
            message.contains("User already registered", ignoreCase = true) -> R.string.auth_error_user_exists
            message.contains("Password should be at least", ignoreCase = true) -> R.string.auth_error_password_too_short
            message.contains("rate limit", ignoreCase = true) ||
                message.contains("too many requests", ignoreCase = true) -> R.string.auth_error_rate_limit
            message.contains("network", ignoreCase = true) ||
                message.contains("Unable to connect", ignoreCase = true) -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
        return context.getString(res)
    }
}
