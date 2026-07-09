package com.penaltycoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.MatchViewModel
import com.penaltycoach.app.ui.navigation.PenaltyNavHost
import com.penaltycoach.app.ui.theme.PenaltyCoachTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Custom orange splash (see themes.xml) shown until stored state loads.
        val splashScreen = installSplashScreen()
        val ready = mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !ready.value }

        super.onCreate(savedInstanceState)

        setContent {
            PenaltyCoachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Both ViewModels are hosted at the activity level and shared
                    // across all destinations, keeping a single source of truth.
                    val appViewModel: AppViewModel = viewModel()
                    val matchViewModel: MatchViewModel = viewModel()

                    val isReady by appViewModel.ready.collectAsStateWithLifecycle()
                    ready.value = isReady

                    if (isReady) {
                        PenaltyNavHost(
                            appViewModel = appViewModel,
                            matchViewModel = matchViewModel
                        )
                    }
                }
            }
        }
    }
}
