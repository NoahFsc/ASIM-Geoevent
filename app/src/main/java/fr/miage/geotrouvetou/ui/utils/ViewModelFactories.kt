package fr.miage.geotrouvetou.ui.utils

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.ui.events.EventDetailViewModel
import fr.miage.geotrouvetou.ui.events.EventFormViewModel

fun appViewModelFactory(context: Context): ViewModelProvider.Factory {
    val app = context.applicationContext as App
    return viewModelFactory {
        initializer { EventFormViewModel(app, app.databaseService, app.authService) }
        initializer { EventDetailViewModel(app.databaseService, app.authService) }
    }
}
