package fr.miage.geotrouvetou.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.User
import fr.miage.geotrouvetou.ui.components.molecules.AdminUserCard
import fr.miage.geotrouvetou.ui.components.organisms.SearchBar

@Composable
internal fun UsersTab(
    users: List<User>,
    currentUserId: String,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onRoleChange: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, users) {
        if (query.isBlank()) users
        else users.filter {
            it.fullName.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
    }
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= users.size - 3 && hasMore && !isLoading
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) onLoadMore() }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SearchBar(value = query, onValueChange = { query = it }, placeholder = stringResource(R.string.admin_search_user))

        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { user ->
                AdminUserCard(
                    name = user.fullName.ifEmpty { user.email },
                    email = user.email,
                    role = user.role.ifEmpty { "user" },
                    isCurrentUser = user.id == currentUserId,
                    onRoleChange = {
                        onRoleChange(user.id, if (user.role == "admin") "user" else "admin")
                    },
                    onDelete = { onDelete(user.id) },
                )
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.primary_500), modifier = Modifier.size(24.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
