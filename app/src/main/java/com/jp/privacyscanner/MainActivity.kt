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
import com.jp.privacyscanner.ui.detail.AppDetailScreen
import com.jp.privacyscanner.ui.home.HomeScreen
import com.jp.privacyscanner.ui.home.HomeViewModel
import com.jp.privacyscanner.ui.theme.PrivacyScannerTheme

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
    const val HOME = "home"
    const val DETAIL = "detail/{packageName}"
    fun detail(packageName: String) = "detail/$packageName"
}

@Composable
private fun PrivacyScannerNav() {
    val navController = rememberNavController()
    // Uma única HomeViewModel partilhada entre os ecrãs — mantém o resultado
    // do scan disponível ao abrir o detalhe sem repetir a análise.
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onAppClick = { pkg -> navController.navigate(Routes.detail(pkg)) }
            )
        }
        composable(Routes.DETAIL) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            AppDetailScreen(
                app = homeViewModel.findApp(pkg),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
