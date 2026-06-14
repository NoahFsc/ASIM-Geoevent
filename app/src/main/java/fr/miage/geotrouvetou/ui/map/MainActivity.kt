package fr.miage.geotrouvetou.ui.map

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import fr.miage.geotrouvetou.ui.utils.SystemStatusToasts
import fr.miage.geotrouvetou.ui.navigation.NavGraph

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavGraph(navController = navController)
            SystemStatusToasts()
        }
    }
}
