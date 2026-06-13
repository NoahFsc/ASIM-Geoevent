package fr.miage.geotrouvetou.ui.utils

import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.components.atoms.TagStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Dérive le statut d'affichage à partir de la date de l'événement. */
fun Evenement.tagStatus(): TagStatus {
    val dateStr = event_date ?: return TagStatus.NEW
    return try {
        val eventDate = LocalDate.parse(dateStr.substringBefore('T'))
        val today = LocalDate.now()
        when {
            eventDate.isBefore(today) -> TagStatus.DONE
            eventDate.isBefore(today.plusDays(7)) -> TagStatus.SOON
            else -> TagStatus.NEW
        }
    } catch (e: Exception) {
        TagStatus.NEW
    }
}

/** "2024-04-28" ou "—" si pas de date. */
fun Evenement.formattedDate(): String = event_date?.substringBefore('T') ?: "—"

/** "09:00" ou "—" si pas d'heure. */
fun Evenement.formattedTime(): String =
    event_date?.let { if ('T' in it) it.substringAfter('T').take(5) else null } ?: "—"

/** "Dimanche, 28 Avril" ou "—" si pas de date. */
fun Evenement.formattedDateLong(): String {
    val dateStr = event_date ?: return "—"
    return try {
        val dt = LocalDateTime.parse(dateStr.take(19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        dt.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale.getDefault()))
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        "—"
    }
}

/** "28/04" ou "—" si pas de date. */
fun Evenement.formattedDateShort(): String {
    val dateStr = event_date ?: return "—"
    return try {
        LocalDate.parse(dateStr.substringBefore('T'))
            .format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (e: Exception) {
        "—"
    }
}
