package fr.miage.geotrouvetou.ui.map.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.domain.models.Place
import fr.miage.geotrouvetou.ui.components.molecules.EventCard
import fr.miage.geotrouvetou.ui.components.molecules.PlaceSearchBar
import fr.miage.geotrouvetou.ui.components.organisms.Modal
import fr.miage.geotrouvetou.ui.components.organisms.SearchBar
import fr.miage.geotrouvetou.ui.utils.formattedDate
import fr.miage.geotrouvetou.ui.utils.formattedTime
import fr.miage.geotrouvetou.ui.utils.tagStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListModal(
    events: List<Evenement>,
    onDismissRequest: () -> Unit,
    title: String = "Propositions",
    onPlaceSelected: ((Place) -> Unit)? = null,
    onEditClick: ((Evenement) -> Unit)? = null,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedEvent by remember { mutableStateOf<Evenement?>(null) }

    Modal(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        if (selectedEvent == null) {
            EventListContent(
                events = events,
                title = title,
                onEventClick = { selectedEvent = it },
                onPlaceSelected = onPlaceSelected
            )
        } else {
            EventDetailModalContentWithViewModel(
                event = selectedEvent!!,
                onBackClick = { selectedEvent = null },
                onEditClick = {
                    onEditClick?.invoke(selectedEvent!!)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun EventListContent(
    events: List<Evenement>,
    onEventClick: (Evenement) -> Unit,
    title: String = "Propositions",
    onPlaceSelected: ((Place) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val isPlaceSearch = onPlaceSelected != null && searchQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Rechercher un lieu"
        )

        if (isPlaceSearch) {
            PlaceSearchBar(
                query = searchQuery,
                onPlaceSelected = onPlaceSelected
            )
        } else {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_darker)
            )

            if (events.isEmpty()) {
                Text(
                    text = "Aucun événement dans cette zone",
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_light)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        tag = event.tagStatus(),
                        title = event.title,
                        date = event.formattedDate(),
                        time = event.formattedTime(),
                        isRecommended = false,
                        attendance = null,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventListContentPreview() {
    Box(modifier = Modifier.background(colorResource(R.color.background))) {
        EventListContent(events = emptyList(), onEventClick = {})
    }
}
