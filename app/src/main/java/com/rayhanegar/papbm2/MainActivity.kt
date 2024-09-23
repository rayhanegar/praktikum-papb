package com.rayhanegar.papbm2

import android.os.Bundle
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
import com.rayhanegar.papbm2.ui.theme.PapbM2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PapbM2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyScreen()
                }
            }
        }
    }
}

@Composable
fun MyScreen() {

    var nameText = remember { mutableStateOf("") }
    var inputNameText = remember { mutableStateOf("") }
    var nimText = remember { mutableStateOf("") }
    var inputNimText = remember { mutableStateOf("") }

    val isButtonEnabled = remember(inputNameText, inputNimText) {
        derivedStateOf {
            inputNameText.value.isNotBlank() && inputNimText.value.isNotBlank()
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
        Text(text = nameText.value,
            style = MaterialTheme.typography.titleLarge)
        Text(text = nimText.value,
            style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = inputNameText.value,
            onValueChange = { inputNameText.value = it },
            label = { Text("Your Name") },
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = "Person icon")
            }
        )
        TextField(
            value = inputNimText.value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    inputNimText.value = newValue
                }
            },
            label = { Text("Your NIM") },
            leadingIcon = {
                Icon(Icons.Outlined.Info, contentDescription = "NIM Info icon")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            nameText.value = inputNameText.value
            nimText.value = inputNimText.value },
            enabled = isButtonEnabled.value,
            modifier = Modifier.pointerInput(Unit){
                detectTapGestures(
                    onLongPress = {
                        Toast
                            .makeText(
                                context,
                                "Hello, ${inputNameText.value}! Your NIM is ${inputNimText.value}", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
        )
        { Text(text = "Pop",
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PapbM2Theme {
        MyScreen()
    }
}
