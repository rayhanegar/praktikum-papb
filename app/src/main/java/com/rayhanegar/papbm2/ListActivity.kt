package com.rayhanegar.papbm2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    ListActivityScreen()
                }
            }
        }
    }
}

@Composable
fun ListActivityScreen() {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dataListState.value) { data -> // Fixed
            DataCard(data)
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
