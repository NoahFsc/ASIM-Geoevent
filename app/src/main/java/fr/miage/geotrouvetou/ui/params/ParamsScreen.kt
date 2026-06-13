package fr.miage.geotrouvetou.ui.params

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ButtonVariant
import fr.miage.geotrouvetou.ui.components.organisms.Modal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onEditPasswordClick: () -> Unit = {},
    viewModel: ParamsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSigningOut by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(
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
            Text(
                text = stringResource(R.string.action_back),
                fontSize = 16.sp,
                color = colorResource(R.color.text_darker),
            )
        }

        Text(
            text = stringResource(R.string.params_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_darker),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.params_section_account),
                fontSize = 14.sp,
                color = colorResource(R.color.text_lighter),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = buildList {
                    add(Triple(Icons.Outlined.Edit, stringResource(R.string.params_edit_profile), onEditProfileClick))
                    add(Triple(Icons.Outlined.Key, stringResource(R.string.params_edit_password), onEditPasswordClick))
                    if (uiState.isAdmin) {
                        add(Triple(Icons.Outlined.Build, stringResource(R.string.params_admin), onAdminClick))
                    }
                }
                items.forEach { (icon, label, action) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White),
                    ) {
                        Button(
                            text = label,
                            onClick = action,
                            leftIcon = icon,
                            rightIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            variant = ButtonVariant.GhostDark,
                            fullWidth = true,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = colorResource(R.color.danger_500),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        Text(
            text = stringResource(R.string.params_logout),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.danger_500),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showLogoutDialog = true },
        )
    }

    if (showLogoutDialog) {
        Modal(onDismissRequest = { showLogoutDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.params_logout_confirm_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 26.sp,
                )
                Text(
                    text = stringResource(R.string.params_logout_confirm_desc),
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_lighter),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    text = stringResource(R.string.params_logout_confirm),
                    onClick = {
                        showLogoutDialog = false
                        isSigningOut = true
                        scope.launch {
                            val success = viewModel.signOut()
                            isSigningOut = false
                            if (success) onLogout()
                        }
                    },
                    enabled = !isSigningOut,
                    variant = ButtonVariant.Fill,
                )
                Text(
                    text = stringResource(R.string.action_back),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.text_darker),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showLogoutDialog = false },
                )
            }
        }
    }
}
