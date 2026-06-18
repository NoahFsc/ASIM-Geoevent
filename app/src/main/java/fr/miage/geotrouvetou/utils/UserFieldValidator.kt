package fr.miage.geotrouvetou.utils

import android.content.Context
import android.util.Patterns
import fr.miage.geotrouvetou.R

object UserFieldValidator {

    fun validateEmail(context: Context, email: String): String? = when {
        email.isBlank() -> context.getString(R.string.validation_email_required)
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> context.getString(R.string.validation_email_invalid)
        else -> null
    }

    fun isLastNameValid(lastName: String) = lastName.isNotBlank()
    fun isFirstNameValid(firstName: String) = firstName.isNotBlank()
    fun isEmailValid(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun capitalizeFirst(value: String) = value.replaceFirstChar { it.uppercaseChar() }
}
