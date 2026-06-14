package fr.miage.geotrouvetou.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Place
import kotlinx.coroutines.delay

@Composable
fun PlaceSearchBar(
    query: String,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    val geocodingService = (LocalContext.current.applicationContext as App).geocodingService
    var placeResults by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            placeResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        delay(500)
        isSearching = true
        placeResults = try {
            geocodingService.search(query)
        } catch (_: Exception) {
            emptyList()
        } finally {
            isSearching = false
        }
    }

    if (query.isBlank()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.place_search_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_darker)
        )

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = colorResource(R.color.primary_400)
                )
            }
        } else if (placeResults.isEmpty()) {
            Text(
                text = stringResource(R.string.place_search_empty),
                fontSize = 14.sp,
                color = colorResource(R.color.text_light)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                placeResults.forEach { place ->
                    PlaceResultCard(
                        place = place,
                        onClick = { onPlaceSelected(place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceResultCard(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.white))
            .border(1.dp, colorResource(R.color.text_disabled), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = colorResource(R.color.primary_400),
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = place.mainLine,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.text_darker),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (place.countryLine.isNotBlank()) {
                    Text(
                        text = place.countryLine,
                        fontSize = 12.sp,
                        color = colorResource(R.color.text_light),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colorResource(R.color.text_light),
            modifier = Modifier.size(20.dp)
        )
    }
}
