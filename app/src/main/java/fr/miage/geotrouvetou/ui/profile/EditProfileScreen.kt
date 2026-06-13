package fr.miage.geotrouvetou.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ButtonVariant
import fr.miage.geotrouvetou.ui.components.atoms.Input
import fr.miage.geotrouvetou.ui.components.atoms.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
            bytes?.let { b -> viewModel.updateAvatar(b) }
        }
    }

    LaunchedEffect(uiState.navigateToLogout) {
        if (uiState.navigateToLogout) {
            onAccountDeleted()
            viewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Retour
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
                    text = "Retour",
                    fontSize = 16.sp,
                    color = colorResource(R.color.text_darker),
                )
            }

            // Titre
            Text(
                text = "Modifier mon profil",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker),
            )

            ProfileAvatarSection(
                avatarUrl = uiState.avatarUrl,
                isUploading = uiState.isUploadingAvatar,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )

            // Carte des informations personnelles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = colorResource(R.color.primary_500),
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "Informations Personnelles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorResource(R.color.text_darker),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Input(
                        value = uiState.nom,
                        onValueChange = viewModel::onNomChange,
                        placeholder = "Nom",
                        label = "Nom",
                        required = true,
                        modifier = Modifier.weight(1f),
                    )
                    Input(
                        value = uiState.prenom,
                        onValueChange = viewModel::onPrenomChange,
                        placeholder = "Prénom",
                        label = "Prénom",
                        required = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                Input(
                    value = uiState.email,
                    onValueChange = {},
                    placeholder = "adresse@email.com",
                    label = "Adresse email",
                    required = true,
                    readOnly = true,
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = colorResource(R.color.danger_500),
                    fontSize = 14.sp,
                )
            }
        }

        // Boutons en bas
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                text = "Supprimer mon compte",
                onClick = { showDeleteDialog = true },
                variant = ButtonVariant.GhostDanger,
            )
            Button(
                text = "Enregistrer les modifications",
                onClick = { viewModel.save() },
                variant = ButtonVariant.Fill,
                enabled = uiState.hasChanges && uiState.formValid && !uiState.isSaving,
                leftIcon = Icons.Outlined.Save,
            )
        }
    }

    if (uiState.saveToastKey > 0) {
        Toast(
            title = "Profil mis à jour !",
            description = "Vos informations ont été enregistrées",
            key = uiState.saveToastKey,
        )
    }
    if (uiState.avatarToastKey > 0) {
        Toast(
            title = "Photo mise à jour !",
            description = "Votre photo de profil a été modifiée",
            key = uiState.avatarToastKey,
        )
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorResource(R.color.primary_500))
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
