package com.rayhanegar.papbm2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.rayhanegar.papbm2.ui.theme.PapbM2Theme

class GithubActivity : ComponentActivity() {
    private val githubProfileViewModel: GithubProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        githubProfileViewModel.fetchGithubProfile("rayhanegar")
        setContent {
            PapbM2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GithubScreen(viewModel = githubProfileViewModel)
                }
            }
        }
    }
}

@Composable
fun GithubScreen(viewModel: GithubProfileViewModel) {
    val githubProfileState by viewModel.githubProfileState.collectAsState()

    if (githubProfileState != null) {
        GithubProfileContent(githubProfile = githubProfileState!!)
    } else {
        Text(text = "Woops, can't display Github Profile.")
    }
}

@Composable
fun GithubProfileContent(githubProfile: GithubProfile) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // Profile pic
        Image(
            painter = rememberAsyncImagePainter(model = githubProfile.avatar_url),
            contentDescription = "GitHub Profile Avatar",
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp),
            contentScale = ContentScale.Crop
        )

        // Username
        Text(text = githubProfile.login,
            style = MaterialTheme.typography.displayLarge)

        // Followers
        Text(text = "Followers: ${githubProfile.followers}")

        // Following
        Text(text = "Following: ${githubProfile.following}")
    }
}

