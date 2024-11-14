package com.rayhanegar.papbm2.screen

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.Locale

@Composable
fun TugasScreen(tugasRepository: TugasRepository) {
    val mainViewModel : MainViewModel = viewModel(factory = MainViewModelFactory(tugasRepository))
    var matkul by remember { mutableStateOf("") }
    var detailTugas by remember { mutableStateOf("") }
    val tugasList by mainViewModel.tugasList.observeAsState(emptyList())
    var namaGambar by remember { mutableStateOf(TextFieldValue("")) }
    var isCameraOpen by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var submissionStatus by remember { mutableStateOf("") }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isCameraOpen = true // Buka kamera jika izin diberikan
        } else {
            submissionStatus = "Izin kamera tidak diberikan."
        }
    }

    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    fun showSnackbar(message: String) {
        snackbarMessage = message
        snackbarVisible = true
    }

    Column(modifier = Modifier.fillMaxWidth()){

        if(isCameraOpen) {
            // CameraX Section
            CameraPreview(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    isCameraOpen = false
                    Log.e("CameraX", "CameraX successfully opened")
                },
                onError = { exception ->
                    submissionStatus = "Failed to open camera."
                    isCameraOpen = false
                    Log.e("CameraX", "CameraX Error", exception)
                }
            )
        } else {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ){
                        Text(text = "Tambahkan!")
                    }

                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                isCameraOpen = true
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text("Mulai Kamera")
                    }
                }
            }

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
fun CameraPreview(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? = remember { null }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Button to capture
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            IconButton(
                onClick = {
                    val photoFile = File(
                        context.externalCacheDir,
                        "photo_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                // Simpan gambar ke galeri
                                saveImageToGallery(context, savedUri)
                                // Callback untuk hasil yang disimpan
                                onImageCaptured(savedUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(8.dp)
            ) {
                // Shutter button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray, shape = CircleShape)
                )
            }
        }
    }
}


fun saveImageToGallery(context: Context, imageUri: Uri) {
    val contentResolver = context.contentResolver
    val fileName = "photo_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
    }

    val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val outputStream: OutputStream? = contentResolver.openOutputStream(it)
        inputStream?.copyTo(outputStream!!)
        inputStream?.close()
        outputStream?.close()
    }
}