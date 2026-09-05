package com.jp.privacyscanner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jp.privacyscanner.ui.detail.AppDetailScreen
import com.jp.privacyscanner.ui.home.HomeScreen
import com.jp.privacyscanner.ui.home.HomeViewModel
import com.jp.privacyscanner.ui.onboarding.OnboardingScreen
import com.jp.privacyscanner.ui.settings.SettingsScreen
import com.jp.privacyscanner.ui.theme.PrivacyScannerTheme
import com.jp.privacyscanner.util.AppPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerNav()
                }
            }
        }
    }
}

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DETAIL = "detail/{packageName}"
    const val SETTINGS = "settings"
    fun detail(packageName: String) = "detail/$packageName"
}

@Composable
private fun ScannerNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val homeViewModel: HomeViewModel = viewModel()

    val start = if (prefs.onboardingCompleted) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = {
                    prefs.onboardingCompleted = true
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            val notifLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* resultado ignorado: o toggle já refletiu a escolha */ }

            HomeScreen(
                viewModel = homeViewModel,
                onAppClick = { pkg -> navController.navigate(Routes.detail(pkg)) },
                onToggleMonitoring = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    homeViewModel.toggleMonitoring(enabled)
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.DETAIL) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            AppDetailScreen(
                app = homeViewModel.findApp(pkg),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
