package fr.miage.geotrouvetou.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import fr.miage.geotrouvetou.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Modal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(64.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colorResource(R.color.text_disabled))
            )
        },
        containerColor = colorResource(R.color.background),
        modifier = modifier
    ) {
        content()
    }
}
