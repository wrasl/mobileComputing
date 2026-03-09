@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapp

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapp.data.MessageDao
import com.example.myapp.data.MessageEntity
import com.example.myapp.data.PhotoDao
import com.example.myapp.data.PhotoEntity
import com.example.myapp.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun CameraScreen(navController: NavController, photoDao: PhotoDao, messageDao: MessageDao, userDao: UserDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }
    
    // Load the user from Room
    val user = userDao.getUser()
    val currentUsername = user?.username ?: "Doge"
    val currentProfileImage = user?.imagePath

    // Observe photos from the database
    val photos by photoDao.getAllPhotos().collectAsState(initial = emptyList())
    var showGallery by remember {
        mutableStateOf(false)
    }

    fun savePhotoToDiskAndDb(bitmap: Bitmap) {
        scope.launch(Dispatchers.IO) {
            try {
                // Filename
                val filename = "captured_photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, filename)
                
                // Save Bitmap to disk
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                // Save the image in the database
                photoDao.insertPhoto(PhotoEntity(filePath = file.absolutePath))

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Camera", "Error saving photo", e)
            }
        }
    }

    fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    onPhotoTaken(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Camera", "Failed to take a photo: ${exception.message}", exception)
                }
            }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = { /*Not used in this project but needed for BottomSheetScaffold*/}
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                },
                modifier = Modifier.offset(15.dp, 25.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        takePhoto(controller) { bitmap ->
                            savePhotoToDiskAndDb(bitmap)
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(
                    onClick = {
                        showGallery = true
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            if (showGallery) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .align(Alignment.BottomCenter)
                ) {
                    Column {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            items(photos) { photoEntity ->
                                Image(
                                    painter = rememberAsyncImagePainter(File(photoEntity.filePath)),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clickable {
                                            scope.launch(Dispatchers.IO) {
                                                messageDao.insertMessage(
                                                    MessageEntity(
                                                        author = currentUsername,
                                                        body = null,
                                                        profileImage = currentProfileImage,
                                                        imagePath = photoEntity.filePath
                                                    )
                                                )
                                                withContext(Dispatchers.Main) {
                                                    navController.popBackStack()
                                                }
                                            }
                                        }
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                showGallery = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Close Gallery",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}
