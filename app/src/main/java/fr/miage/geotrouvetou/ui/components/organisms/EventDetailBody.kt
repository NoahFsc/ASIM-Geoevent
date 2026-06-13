package fr.miage.geotrouvetou.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import fr.miage.geotrouvetou.BuildConfig
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.molecules.EventDetailCard

@Composable
fun EventDetailBody(
    imageUrl: String?,
    date: String,
    time: String,
    locationName: String,
    locationDetail: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorResource(R.color.text_disabled))
        )

        EventDetailCard(
            date = date,
            time = time,
            locationName = locationName,
            locationDetail = locationDetail
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.event_detail_about),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker)
            )
            Text(
                text = description,
                fontSize = 16.sp,
                color = colorResource(R.color.text_lighter),
                lineHeight = 24.sp
            )
        }
    }
}
