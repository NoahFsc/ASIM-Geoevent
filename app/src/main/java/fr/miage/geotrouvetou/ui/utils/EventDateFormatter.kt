package fr.miage.geotrouvetou.ui.utils

import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.components.atoms.TagStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Evenement.tagStatus(): TagStatus {
    val dateStr = eventDate ?: return TagStatus.NEW
    return try {
        val eventDate = LocalDate.parse(dateStr.substringBefore('T'))
        val today = LocalDate.now()
        when {
            eventDate.isBefore(today) -> TagStatus.DONE
            eventDate.isBefore(today.plusDays(7)) -> TagStatus.SOON
            else -> TagStatus.NEW
        }
    } catch (_: Exception) {
        TagStatus.NEW
    }
}

fun Evenement.formattedDate(): String = eventDate?.substringBefore('T') ?: "—"

fun Evenement.formattedTime(): String =
    eventDate?.let { if ('T' in it) it.substringAfter('T').take(5) else null } ?: "—"

fun Evenement.formattedDateLong(): String {
    val dateStr = eventDate ?: return "—"
    return try {
        val dt = LocalDateTime.parse(dateStr.take(19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        dt.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale.getDefault()))
            .replaceFirstChar { it.uppercase() }
    } catch (_: Exception) {
        "—"
    }
}

fun Evenement.formattedDateShort(): String {
    val dateStr = eventDate ?: return "—"
    return try {
        LocalDate.parse(dateStr.substringBefore('T'))
            .format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (_: Exception) {
        "—"
    }
}
