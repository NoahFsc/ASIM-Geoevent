package fr.miage.geotrouvetou.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.SegmentedControl

@Composable
fun AdminScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onBackClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colorResource(R.color.text_darker),
                    modifier = Modifier.size(20.dp),
                )
                Text(text = stringResource(R.string.action_back), fontSize = 16.sp, color = colorResource(R.color.text_darker))
            }

            Text(
                text = stringResource(R.string.admin_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )

            SegmentedControl(
                tabs = listOf(
                    stringResource(R.string.admin_tab_overview),
                    stringResource(R.string.admin_tab_users),
                    stringResource(R.string.admin_tab_events),
                ),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }

        when (selectedTab) {
            0 -> ApercuTab(
                stats = uiState.stats,
                recentActivity = uiState.recentActivity,
                isLoadingStats = uiState.isLoadingStats,
                isLoadingActivity = uiState.isLoadingActivity,
                onRefresh = { viewModel.loadStats(); viewModel.loadRecentActivity() },
            )
            1 -> UsersTab(
                users = uiState.users,
                currentUserId = uiState.currentUserId,
                isLoading = uiState.isLoadingUsers,
                hasMore = uiState.hasMoreUsers,
                onLoadMore = viewModel::loadMoreUsers,
                onRoleChange = { userId, newRole -> viewModel.updateUserRole(userId, newRole) },
                onDelete = { userId -> viewModel.deleteUser(userId) },
            )
            2 -> EventsTab(
                events = uiState.events,
                isLoading = uiState.isLoadingEvents,
                hasMore = uiState.hasMoreEvents,
                onLoadMore = viewModel::loadMoreEvents,
                onDelete = { eventId -> viewModel.deleteEvent(eventId) },
            )
        }
    }
}
