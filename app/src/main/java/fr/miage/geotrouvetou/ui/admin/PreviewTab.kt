package fr.miage.geotrouvetou.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.AdminStats
import fr.miage.geotrouvetou.domain.models.AuditLogEntry
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun relativeTime(context: android.content.Context, isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(isoDate.take(19)) ?: return isoDate.take(10)
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000L -> context.getString(R.string.admin_time_now)
            diff < 3_600_000L -> context.getString(R.string.admin_time_minutes, (diff / 60_000).toInt())
            diff < 86_400_000L -> context.getString(R.string.admin_time_hours, (diff / 3_600_000).toInt())
            else -> context.getString(R.string.admin_time_days, (diff / 86_400_000).toInt())
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

@Composable
internal fun ApercuTab(
    stats: AdminStats,
    recentActivity: List<AuditLogEntry>,
    isLoadingStats: Boolean,
    isLoadingActivity: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isLoadingStats) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(R.color.primary_500))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = { Icon(Icons.Outlined.Group, null, tint = colorResource(R.color.primary_600), modifier = Modifier.size(20.dp)) },
                    count = stats.userCount.toString(),
                    label = stringResource(R.string.admin_stat_users),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = colorResource(R.color.primary_600), modifier = Modifier.size(20.dp)) },
                    count = stats.eventCount.toString(),
                    label = stringResource(R.string.admin_stat_events),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.admin_recent_activity),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )
            Text(
                text = stringResource(R.string.action_refresh),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.primary_500),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onRefresh() },
            )
        }

        when {
            isLoadingActivity -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(R.color.primary_500), modifier = Modifier.size(24.dp))
            }
            recentActivity.isEmpty() -> Text(
                text = stringResource(R.string.admin_no_activity),
                fontSize = 14.sp,
                color = colorResource(R.color.text_lighter),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
            ) {
                recentActivity.forEachIndexed { i, entry ->
                    ActivityRow(entry)
                    if (i < recentActivity.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colorResource(R.color.text_disabled),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    count: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.primary_transparent)),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.text_darker))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorResource(R.color.text_lighter), letterSpacing = 0.5.sp)
    }
}

private data class ActionStyle(val icon: ImageVector, val colorRes: Int, val label: String)

@Composable
private fun ActivityRow(entry: AuditLogEntry) {
    val name = entry.targetName.orEmpty().ifEmpty { "—" }
    val newRole = entry.metadata?.get("new_role")?.toString()?.trim('"')
    val style = when (entry.action) {
        "user_created"  -> ActionStyle(Icons.Filled.PersonAdd,   R.color.success_400, "Nouvel utilisateur : $name")
        "user_deleted"  -> ActionStyle(Icons.Outlined.PersonOff, R.color.danger_400,  "Compte supprimé : $name")
        "role_changed"  -> ActionStyle(Icons.Filled.Shield,      R.color.primary_400, "$name est maintenant ${newRole ?: "?"}")
        "event_created" -> ActionStyle(Icons.Filled.Flag,        R.color.primary_400, "Événement créé : « $name »")
        "event_deleted" -> ActionStyle(Icons.Outlined.Flag,      R.color.warning_400, "Événement supprimé : « $name »")
        else            -> ActionStyle(Icons.Filled.Flag,        R.color.text_lighter, entry.action)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(style.colorRes).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = style.icon, contentDescription = null, tint = colorResource(style.colorRes), modifier = Modifier.size(18.dp))
        }
        Text(text = style.label, fontSize = 13.sp, color = colorResource(R.color.text_darker), lineHeight = 18.sp, modifier = Modifier.weight(1f))
        Text(text = relativeTime(LocalContext.current, entry.createdAt), fontSize = 11.sp, color = colorResource(R.color.text_placeholder))
    }
}
