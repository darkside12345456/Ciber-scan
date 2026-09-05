package com.jp.privacyscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jp.privacyscanner.ui.bugbounty.BountyListScreen
import com.jp.privacyscanner.ui.bugbounty.BountyProgramScreen
import com.jp.privacyscanner.ui.bugbounty.BountyViewModel
import com.jp.privacyscanner.ui.bugbounty.FindingEditorScreen
import com.jp.privacyscanner.ui.settings.SettingsScreen
import com.jp.privacyscanner.ui.theme.PrivacyScannerTheme

class BountyMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BountyNav()
                }
            }
        }
    }
}

private object Routes {
    const val LIST = "bounty"
    const val PROGRAM = "bounty/program/{programId}"
    const val FINDING = "bounty/finding/{programId}/{findingId}"
    const val SETTINGS = "settings"
    fun program(id: Long) = "bounty/program/$id"
    fun finding(programId: Long, findingId: Long) = "bounty/finding/$programId/$findingId"
}

@Composable
private fun BountyNav() {
    val navController = rememberNavController()
    val bountyViewModel: BountyViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            BountyListScreen(
                viewModel = bountyViewModel,
                onProgramClick = { id -> navController.navigate(Routes.program(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                bottomBar = {}
            )
        }
        composable(Routes.PROGRAM) { backStackEntry ->
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
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
