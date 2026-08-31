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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jp.privacyscanner.ui.bugbounty.BountyListScreen
import com.jp.privacyscanner.ui.bugbounty.BountyProgramScreen
import com.jp.privacyscanner.ui.bugbounty.BountyViewModel
import com.jp.privacyscanner.ui.bugbounty.FindingEditorScreen
import com.jp.privacyscanner.ui.detail.AppDetailScreen
import com.jp.privacyscanner.ui.home.HomeScreen
import com.jp.privacyscanner.ui.home.HomeViewModel
import com.jp.privacyscanner.ui.onboarding.OnboardingScreen
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
                    PrivacyScannerNav()
                }
            }
        }
    }
}

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DETAIL = "detail/{packageName}"
    const val BOUNTY = "bounty"
    const val BOUNTY_PROGRAM = "bounty/program/{programId}"
    const val FINDING = "bounty/finding/{programId}/{findingId}"
    fun detail(packageName: String) = "detail/$packageName"
    fun program(id: Long) = "bounty/program/$id"
    fun finding(programId: Long, findingId: Long) = "bounty/finding/$programId/$findingId"
}

@Composable
private fun PrivacyScannerNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    // ViewModels partilhadas: a Home mantém o resultado do scan entre ecrãs; a
    // Bounty serve todos os ecrãs de bug bounty.
    val homeViewModel: HomeViewModel = viewModel()
    val bountyViewModel: BountyViewModel = viewModel()

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
            // Pedido da permissão de notificações (Android 13+). A monitorização
            // é ativada de qualquer forma; sem a permissão apenas não notifica.
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
                bottomBar = { MainBottomBar(navController) }
            )
        }
        composable(Routes.DETAIL) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            AppDetailScreen(
                app = homeViewModel.findApp(pkg),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BOUNTY) {
            BountyListScreen(
                viewModel = bountyViewModel,
                onProgramClick = { id -> navController.navigate(Routes.program(id)) },
                bottomBar = { MainBottomBar(navController) }
            )
        }
        composable(Routes.BOUNTY_PROGRAM) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L
            BountyProgramScreen(
                programId = id,
                viewModel = bountyViewModel,
                onBack = { navController.popBackStack() },
                onAddFinding = { pid -> navController.navigate(Routes.finding(pid, 0)) },
                onFindingClick = { fid -> navController.navigate(Routes.finding(id, fid)) }
            )
        }
        composable(Routes.FINDING) { backStackEntry ->
            val pid = backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L
            val fid = backStackEntry.arguments?.getString("findingId")?.toLongOrNull() ?: 0L
            FindingEditorScreen(
                programId = pid,
                findingId = fid,
                viewModel = bountyViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/** Barra inferior que alterna entre a área de Privacidade e a de Bug Bounty. */
@Composable
private fun MainBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                if (currentRoute != Routes.HOME) {
                    navController.navigate(Routes.HOME) { launchSingleTop = true }
                }
            },
            icon = { Icon(Icons.Default.Shield, contentDescription = null) },
            label = { Text("Privacidade") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.BOUNTY,
            onClick = {
                if (currentRoute != Routes.BOUNTY) {
                    navController.navigate(Routes.BOUNTY) { launchSingleTop = true }
                }
            },
            icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
            label = { Text("Bug Bounty") }
        )
    }
}
