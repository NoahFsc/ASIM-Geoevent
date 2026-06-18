package fr.miage.geotrouvetou.utils

import android.content.Context
import fr.miage.geotrouvetou.R

data class PasswordValidation(
    val hasMinLength: Boolean,
    val hasDigit: Boolean,
    val hasSpecial: Boolean,
    val hasUppercase: Boolean,
) {
    val isValid: Boolean get() = hasMinLength && hasDigit && hasSpecial && hasUppercase

    fun firstError(context: Context): String? = when {
        !hasMinLength -> context.getString(R.string.validation_password_min)
        !hasDigit -> context.getString(R.string.validation_password_digit)
        !hasUppercase -> context.getString(R.string.validation_password_uppercase)
        !hasSpecial -> context.getString(R.string.validation_password_special)
        else -> null
    }

    companion object {
        val EMPTY = PasswordValidation(
            hasMinLength = false,
            hasDigit = false,
            hasSpecial = false,
            hasUppercase = false,
        )

        fun of(password: String) = PasswordValidation(
            hasMinLength = password.length >= 8,
            hasDigit = password.any { it.isDigit() },
            hasSpecial = password.any { !it.isLetterOrDigit() },
            hasUppercase = password.any { it.isUpperCase() },
        )

        fun passwordsMatch(password: String, confirm: String): Boolean =
            confirm.isNotEmpty() && confirm == password

        fun confirmError(context: Context, password: String, confirm: String): String? =
            if (confirm.isNotEmpty() && confirm != password)
                context.getString(R.string.validation_passwords_mismatch) else null
    }
}
