package fr.miage.geotrouvetou.ui.utils

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.ui.events.EventDetailViewModel
import fr.miage.geotrouvetou.ui.events.EventFormViewModel

/**
 * Factory unique pour les ViewModels à dépendances injectées, alimentée par le
 * ServiceLocator ([App]). Évite de dupliquer l'instanciation des services dans chaque écran.
 */
fun appViewModelFactory(context: Context): ViewModelProvider.Factory {
    val app = context.applicationContext as App
    return viewModelFactory {
        initializer { EventFormViewModel(app, app.databaseService, app.authService) }
        initializer { EventDetailViewModel(app.databaseService, app.authService) }
    }
}
