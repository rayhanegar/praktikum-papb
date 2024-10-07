package com.rayhanegar.papbm2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rayhanegar.papbm2.ui.theme.PapbM2Theme

class ListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PapbM2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListActivityScreen(buttonOnClick = { navigateToGithubActivity() })
                }
            }
        }
    }

    private fun navigateToGithubActivity() {
        try {
            Log.d("ListActivity", "Navigating to GithubActivity")
            val intent = Intent(this, GithubActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("ListActivity", "Navigasi ke GithubActivity gagal: ${e.message}")
        }
    }
}

@Composable
fun ListActivityScreen(buttonOnClick: () -> Unit) {
    val db = Firebase.firestore
    val dataListState = remember { mutableStateOf(listOf<DataModel>()) }

    LaunchedEffect(Unit) {
        db.collection("matkul")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.map { document ->
                    DataModel(
                        nama = document.getString("nama") ?: "-",
                        hari = Hari.valueOf(document.getString("hari")?.uppercase() ?: "-"),
                        jam = document.getString("jam") ?: "-",
                        ruang = document.getString("ruang") ?: "-",
                        praktikum = document.getBoolean("is_praktikum") ?: false
                    )
                }
                dataListState.value = items.sortedWith(
                    compareBy<DataModel> { it.hari.urutan }
                        .thenBy { it.jam }
                )
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GithubButton(
            onClick = buttonOnClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dataListState.value) { data ->
                DataCard(data)
            }
        }
    }
}

@Composable
fun DataCard(data: DataModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nama MK: ${data.nama}", style = MaterialTheme.typography.bodyMedium)
            Text("Hari: ${data.hari}", style = MaterialTheme.typography.bodyMedium)
            Text("Jam: ${data.jam}", style = MaterialTheme.typography.bodyMedium)
            Text("Ruang: ${data.ruang}", style = MaterialTheme.typography.bodyMedium)

            if (data.praktikum) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kelas Praktikum", style = MaterialTheme.typography.bodyMedium, color = Color.Blue)
            }
        }
    }
}

@Composable
fun GithubButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_github_mark),
                contentDescription = "GitHub Icon",
                modifier = Modifier.size(24.dp) // Set size for the icon
            )
            Text(
                text = "View GitHub profile",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

data class DataModel(
    val nama: String,
    val hari: Hari,
    val jam: String,
    val ruang: String,
    val praktikum: Boolean
)

enum class Hari(val urutan: Int) {
    SENIN(1),
    SELASA(2),
    RABU(3),
    KAMIS(4),
    JUMAT(5),
    SABTU(6),
    MINGGU(7)
}
