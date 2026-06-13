package fr.miage.geotrouvetou.utils

import android.util.Patterns

object UserFieldValidator {

    fun validateLastName(lastName: String): String? =
        if (lastName.isBlank()) "Le nom est requis" else null

    fun validateFirstName(firstName: String): String? =
        if (firstName.isBlank()) "Le prénom est requis" else null

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "L'adresse email est requise"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format d'email invalide"
        else -> null
    }

    fun isLastNameValid(lastName: String) = validateLastName(lastName) == null
    fun isFirstNameValid(firstName: String) = validateFirstName(firstName) == null
    fun isEmailValid(email: String) = validateEmail(email) == null

    fun capitalizeFirst(value: String) = value.replaceFirstChar { it.uppercaseChar() }
}
