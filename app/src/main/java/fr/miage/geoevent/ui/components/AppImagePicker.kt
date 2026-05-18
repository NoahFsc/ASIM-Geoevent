package fr.miage.geoevent.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppImagePicker(
    selectedUri: Uri?,
    onUriSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Image",
    placeholder: String = "Cliquez pour choisir une image"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onUriSelected(uri)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { launcher.launch("image/*") }
    ) {
        AppTextField(
            value = if (selectedUri != null) "Image sélectionnée" else "",
            onValueChange = { },
            label = label,
            placeholder = placeholder,
            enabled = false
        )
        // Zone invisible pour intercepter le clic si l'AppTextField est désactivé
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable { launcher.launch("image/*") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppImagePickerPreview() {
    AppImagePicker(
        selectedUri = null,
        onUriSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AppImagePickerSelectedPreview() {
    AppImagePicker(
        selectedUri = Uri.parse("content://dummy"),
        onUriSelected = {}
    )
}