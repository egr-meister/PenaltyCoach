package com.penaltycoach.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.MatchViewModel
import com.penaltycoach.app.ui.screens.DayDetailScreen
import com.penaltycoach.app.ui.screens.HistoryScreen
import com.penaltycoach.app.ui.screens.HomeScreen
import com.penaltycoach.app.ui.screens.MatchScheduleScreen
import com.penaltycoach.app.ui.screens.MatchSettingsScreen
import com.penaltycoach.app.ui.screens.OnboardingScreen
import com.penaltycoach.app.ui.screens.SeriesScreen
import com.penaltycoach.app.ui.screens.SeriesSummaryScreen
import com.penaltycoach.app.ui.screens.SettingsScreen
import com.penaltycoach.app.ui.screens.ShotEditScreen
import com.penaltycoach.app.ui.screens.ShotEntryScreen
import com.penaltycoach.app.ui.screens.StatisticsScreen

/** Central route definitions + argument-safe helpers. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SHOT_ENTRY = "shot_entry"
    const val STATISTICS = "statistics"
    const val HISTORY = "history"
    const val MATCH_SCHEDULE = "match_schedule"
    const val MATCH_SETTINGS = "match_settings"
    const val SETTINGS = "settings"

    const val SERIES = "series/{target}"
    fun series(target: Int) = "series/$target"

    const val SERIES_SUMMARY = "series_summary/{seriesId}"
    fun seriesSummary(seriesId: String) = "series_summary/$seriesId"

    const val DAY_DETAIL = "day/{date}"
    fun dayDetail(date: String) = "day/$date"

    const val SHOT_EDIT = "shot_edit/{shotId}"
    fun shotEdit(shotId: String) = "shot_edit/$shotId"
}

@Composable
fun PenaltyNavHost(
    appViewModel: AppViewModel,
    matchViewModel: MatchViewModel,
    navController: NavHostController = rememberNavController()
) {
    val onboardingDone = appViewModel.state.value.settings.onboardingCompleted
    val start = if (onboardingDone) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    appViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                appViewModel = appViewModel,
                onRecordShot = { navController.navigate(Routes.SHOT_ENTRY) },
                onStartSeries = { target -> navController.navigate(Routes.series(target)) },
                onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenMatches = { navController.navigate(Routes.MATCH_SCHEDULE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SHOT_ENTRY) {
            ShotEntryScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SERIES,
            arguments = listOf(navArgument("target") {
                type = NavType.IntType
                defaultValue = 5
            })
        ) { entry ->
            val target = entry.arguments?.getInt("target") ?: 5
            SeriesScreen(
                appViewModel = appViewModel,
                target = target,
                onBack = { navController.popBackStack() },
                onFinished = { seriesId ->
                    navController.navigate(Routes.seriesSummary(seriesId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.SERIES_SUMMARY,
            arguments = listOf(navArgument("seriesId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            val seriesId = entry.arguments?.getString("seriesId").orEmpty()
            SeriesSummaryScreen(
                appViewModel = appViewModel,
                seriesId = seriesId,
                onBack = { navController.popBackStack() },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY) { popUpTo(Routes.HOME) }
                }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onOpenDay = { date -> navController.navigate(Routes.dayDetail(date)) }
            )
        }

        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(navArgument("date") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            val date = entry.arguments?.getString("date").orEmpty()
            DayDetailScreen(
                appViewModel = appViewModel,
                date = date,
                onBack = { navController.popBackStack() },
                onEditShot = { shotId -> navController.navigate(Routes.shotEdit(shotId)) }
            )
        }

        composable(
            route = Routes.SHOT_EDIT,
            arguments = listOf(navArgument("shotId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            val shotId = entry.arguments?.getString("shotId").orEmpty()
            ShotEditScreen(
                appViewModel = appViewModel,
                shotId = shotId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MATCH_SCHEDULE) {
            MatchScheduleScreen(
                matchViewModel = matchViewModel,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.MATCH_SETTINGS) }
            )
        }

        composable(Routes.MATCH_SETTINGS) {
            MatchSettingsScreen(
                appViewModel = appViewModel,
                matchViewModel = matchViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                appViewModel = appViewModel,
                matchViewModel = matchViewModel,
                onBack = { navController.popBackStack() },
                onOpenMatchSettings = { navController.navigate(Routes.MATCH_SETTINGS) },
                onShowOnboarding = {
                    appViewModel.showOnboardingAgain()
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
