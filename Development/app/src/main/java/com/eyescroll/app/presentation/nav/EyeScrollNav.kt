package com.eyescroll.app.presentation.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eyescroll.app.presentation.HomeViewModel
import com.eyescroll.app.presentation.screens.CalibrationScreen
import com.eyescroll.app.presentation.screens.HomeScreen
import com.eyescroll.app.presentation.screens.PermissionsScreen
import com.eyescroll.app.presentation.screens.TutorialScreen
import com.eyescroll.app.presentation.screens.WelcomeScreen
import org.koin.androidx.compose.koinViewModel

object Routes {
    const val WELCOME = "welcome"
    const val PERMISSIONS = "permissions"
    const val TUTORIAL = "tutorial"
    const val CALIBRATION = "calibration"
    const val HOME = "home"
}

@Composable
fun EyeScrollNav(vm: HomeViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsState()
    val nav = rememberNavController()
    val start = if (state.onboardingDone) Routes.HOME else Routes.WELCOME

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onContinue = { nav.navigate(Routes.PERMISSIONS) })
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onContinue = { nav.navigate(Routes.TUTORIAL) })
        }
        composable(Routes.TUTORIAL) {
            TutorialScreen(onContinue = { nav.navigate(Routes.CALIBRATION) })
        }
        composable(Routes.CALIBRATION) {
            CalibrationScreen(
                onDone = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onSkip = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onBaseline = { vm.setNoseBaseline(it) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(vm = vm)
        }
    }
}
