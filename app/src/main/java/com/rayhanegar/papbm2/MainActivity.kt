package com.rayhanegar.papbm2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rayhanegar.papbm2.navigation.NavigationItem
import com.rayhanegar.papbm2.navigation.Screen
import com.rayhanegar.papbm2.screen.MatkulScreen
import com.rayhanegar.papbm2.screen.ProfileScreen
import com.rayhanegar.papbm2.screen.TugasScreen
import com.rayhanegar.papbm2.ui.theme.PapbM2Theme

class MainActivity : ComponentActivity() {

    private val githubProfileViewModel: GithubProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        githubProfileViewModel.fetchGithubProfile("rayhanegar")
        setContent {
            PapbM2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityScreen(viewModel = githubProfileViewModel)
                }
            }
        }
    }
}

@Composable
fun MainActivityScreen(
    modifier : Modifier = Modifier,
    navController : NavHostController = rememberNavController(),
    viewModel: GithubProfileViewModel

) {
    Scaffold(
        bottomBar = {BottomBar(navController)},
        modifier = modifier
    ) {
        innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Matkul.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Matkul.route) {
                MatkulScreen()
            }
            composable(Screen.Tugas.route) {
                TugasScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel)
            }
        }
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier
    ) {
        val navigationItems = listOf(
            NavigationItem(
                title = "Matkul",
                icon = Icons.Filled.DateRange,
                screen = Screen.Matkul
            ),
            NavigationItem(
                title = "Tugas",
                icon = Icons.Filled.Check,
                screen = Screen.Tugas
            ),
            NavigationItem(
                title = "Profile",
                icon = Icons.Filled.AccountCircle,
                screen = Screen.Profile
            )
        )
        navigationItems.map { item ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = item.icon,
                        contentDescription = item.title)
                },
                label = { Text(text = item.title) },
                selected = false,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}