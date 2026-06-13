package fr.miage.geotrouvetou.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

@Composable
fun AdminUserCard(
    name: String,
    email: String,
    role: String,
    isCurrentUser: Boolean,
    onRoleChange: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.text_disabled)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = colorResource(R.color.text_light),
                modifier = Modifier.size(26.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )
            Text(
                text = email,
                fontSize = 13.sp,
                color = colorResource(R.color.text_lighter),
            )
        }

        val isAdmin = role == "admin"
        Box(
            modifier = Modifier
                .background(
                    colorResource(if (isAdmin) R.color.primary_400 else R.color.text_disabled),
                    RoundedCornerShape(50),
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                text = role.uppercase().ifEmpty { "USER" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(if (isAdmin) R.color.white else R.color.text_light),
            )
        }

        Box {
            IconButton(
                onClick = { if (!isCurrentUser) menuExpanded = true },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.admin_menu_cd),
                    tint = colorResource(if (isCurrentUser) R.color.text_disabled else R.color.text_lighter),
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = if (isAdmin) stringResource(R.string.admin_make_user) else stringResource(R.string.admin_make_admin), fontSize = 14.sp) },
                    onClick = { menuExpanded = false; onRoleChange() },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.admin_delete_user), fontSize = 14.sp, color = colorResource(R.color.danger_500)) },
                    onClick = { menuExpanded = false; onDelete() },
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdminUserCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        AdminUserCard(
            name = "Sara",
            email = "sara.t@gmail.com",
            role = "user",
            isCurrentUser = false,
            onRoleChange = {},
            onDelete = {},
        )
    }
}
