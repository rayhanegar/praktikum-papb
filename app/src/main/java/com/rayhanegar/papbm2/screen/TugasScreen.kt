package com.rayhanegar.papbm2.screen

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rayhanegar.papbm2.data.model.local.TugasRepository
import com.rayhanegar.papbm2.viewmodel.MainViewModel
import com.rayhanegar.papbm2.viewmodel.MainViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.Locale

@Composable
fun TugasScreen(tugasRepository: TugasRepository) {
    val mainViewModel : MainViewModel = viewModel(factory = MainViewModelFactory(tugasRepository))
    var matkul by remember { mutableStateOf("") }
    var detailTugas by remember { mutableStateOf("") }
    val tugasList by mainViewModel.tugasList.observeAsState(emptyList())

    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current

    fun showSnackbar(message: String) {
        snackbarMessage = message
        snackbarVisible = true
    }

    Column(modifier = Modifier.fillMaxWidth()){

        // Section for task input
        Column(modifier = Modifier.weight(1f)){
            Text(text = "Add new Task", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = matkul,
                onValueChange = { matkul = it },
                label = {Text("Mata Kuliah")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = detailTugas,
                onValueChange = { detailTugas = it },
                label = {Text("Detail Tugas")},
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (matkul.isNotEmpty() && detailTugas.isNotEmpty()) {
                        mainViewModel.addTugas(matkul, detailTugas)
                        showSnackbar("Tugas berhasil ditambahkan!")
                        matkul = ""
                        detailTugas = ""
                    } else {
                        showSnackbar("Pastikan Nama dan Detail terisi!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Text(text = "Tambahkan!")
            }
        }

        // CameraX Section
        CameraCaptureScreen(lifecycleOwner = lifecycleOwner)

        // Section for task list
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp)
        ){
            Text(text = "Task List", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))

            if (tugasList.isEmpty()){
                Text(text = "You're free for now.", style = MaterialTheme.typography.titleMedium)
            } else {
                for ((index, tugas) in tugasList.withIndex()) {
                    if (!tugas.selesai) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, // Align items
                                verticalAlignment = Alignment.CenterVertically     // Center vertically
                            ) {
                                Column {
                                    Text(
                                        text = "Mata Kuliah: ${tugas.matkul}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Detail Tugas: ${tugas.detailTugas}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Checkbox(
                                    checked = tugas.selesai,
                                    onCheckedChange = { isChecked ->
                                        mainViewModel.updateTugas(tugas)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (snackbarVisible) {
        Snackbar(
            action = {
                TextButton(onClick = { snackbarVisible = false }) {
                    Text("Okay")
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(snackbarMessage)
        }
    }
}

@Composable
fun CameraCaptureScreen(lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(true) }
    var photoUriState by remember { mutableStateOf<Uri?>(null) }
    var imageCapture: ImageCapture? = remember { null }

    if (showCamera) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    // Preview
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    imageCapture = ImageCapture.Builder().build()

                    // Select back camera as a default
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )

                    } catch (exc: Exception) {
                        Log.e("CameraCaptureScreen", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            update = { previewView ->
                // This is where you can update the previewView, for example, when
                // the device orientation changes.
            }
        )
    } else {
        photoUriState?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {
            val imageCapture = imageCapture ?: return@Button
            val photoFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("CameraCaptureScreen", "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        photoUriState = savedUri
                        showCamera = false
                        Log.d("CameraCaptureScreen", "Photo capture succeeded: $savedUri")
                    }
                }
            )
        }) {
            Text("Take Photo")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            showCamera = true
        }) {
            Text("Camera")
        }
    }
}