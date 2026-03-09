package com.example.myapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapp.data.User
import com.example.myapp.data.UserDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navController: NavController, author: String?, userDao: UserDao) {

    val context = LocalContext.current

    val existingUser = userDao.getUser()

    var savedName by remember {
        mutableStateOf(existingUser?.username ?: author ?: "")
    }

    var savedImage by remember {
        mutableStateOf(existingUser?.imagePath)
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {

            val inputStream = context.contentResolver.openInputStream(it)

            val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            val outputStream = file.outputStream()

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            savedImage = file.absolutePath
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationUtil.showNotification(context)
        } else {
            Toast.makeText(
                context,
                "Permission is required to send notifications.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("conversation") {
                            popUpTo("conversation") {
                                inclusive=true
                            }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // check if there's a saved image we can use
            val imagePath = savedImage

            if (imagePath != null) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profilepic),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Pick Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = savedName,
                onValueChange = { savedName = it },
                label = { Text("username") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {

                val user = User(
                    uid = 0,
                    username = savedName,
                    imagePath = savedImage
                )

                userDao.saveUser(user)
            }) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            NotificationUtil.showNotification(context)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    NotificationUtil.showNotification(context)
                }
            }) {
                Text("Send Notification")
            }

        }
    }
}