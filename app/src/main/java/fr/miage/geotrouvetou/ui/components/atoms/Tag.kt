package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R

enum class TagStatus(val backgroundColorRes: Int) {
    NEW(R.color.success_400),
    SOON(R.color.warning_400),
    DONE(R.color.danger_400)
}

@Composable
fun StatusTag(
    status: TagStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(id = status.backgroundColorRes),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 2.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        val label = when (status) {
            TagStatus.NEW  -> stringResource(R.string.tag_status_new)
            TagStatus.SOON -> stringResource(R.string.tag_status_soon)
            TagStatus.DONE -> stringResource(R.string.tag_status_done)
        }
        Text(
            text = label,
            color = colorResource(id = R.color.text_darker),
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun StatusTagPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        StatusTag(status = TagStatus.NEW)

        Box(modifier = Modifier.padding(top = 16.dp)) {
            StatusTag(status = TagStatus.SOON)
        }

        Box(modifier = Modifier.padding(top = 16.dp)) {
            StatusTag(status = TagStatus.DONE)
        }
    }
}
