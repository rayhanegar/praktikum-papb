package com.rayhanegar.papbm2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rayhanegar.papbm2.ui.theme.PapbM2Theme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            PapbM2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyScreen(auth, loginOnClick = { email, password -> loginWithEmail(email, password) })
                }
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Login Success, navigating to List Activity")
                    navigateToListActivity()
                } else {
                    Log.e("MainActivity", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToListActivity() {
        try {
            Log.d("MainActivity", "Navigating to ListActivity")
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigasi ke ListActivity gagal: ${e.message}")
        }
    }

}


@Composable
fun MyScreen(auth: FirebaseAuth, loginOnClick: (String, String) -> Unit) {

    var emailText = remember { mutableStateOf("") }
    var passwordText = remember { mutableStateOf("") }

    val isButtonEnabled = remember(emailText, passwordText) {
        derivedStateOf {
            emailText.value.isNotBlank() && passwordText.value.isNotBlank()
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Page",
            style = MaterialTheme.typography.titleLarge)
        Text(text = "Use your email and password",
            style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = emailText.value,
            onValueChange = { emailText.value = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = "Person icon")
            }
        )
        TextField(
            value = passwordText.value,
            onValueChange = { passwordText.value = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Info, contentDescription = "Password icon")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginOnClick(emailText.value, passwordText.value) },
            enabled = isButtonEnabled.value
        )
        { Text(text = "Login",
                style = MaterialTheme.typography.labelSmall)
        }
    }

}
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    PapbM2Theme {
//        MyScreen(auth = Firebase.auth)
//    }
//}
