package com.example.myapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.MyAppTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.room.Room
import com.example.myapp.data.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createNotificationChannels(this)
        }

        setContent {
            MyAppTheme {

                val requestPermissionLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        startRotationService()
                    } else {
                        Toast.makeText(
                            this,
                            "Permission is required to detect device rotation.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // This effect runs when the composable is first launched
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) -> {
                                startRotationService()
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        // On older versions, no runtime permission is needed
                        startRotationService()
                    }
                }

                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                )
                    .allowMainThreadQueries()
                    .build()

                val userDao = db.userDao()

                if (userDao.getUser() == null) {
                    userDao.saveUser(User(uid = 0, username = "Doge", imagePath = null))
                }

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "conversation") {
                    composable("conversation") {
                        ConversationScreen(navController = navController, userDao = userDao)
                    }
                    composable("details/{author}") { backStackEntry ->
                        val author = backStackEntry.arguments?.getString("author")
                        DetailsScreen(
                            navController = navController,
                            author = author,
                            userDao = userDao
                        )
                    }
                }
            }
        }
    }

    private fun startRotationService() {
        val serviceIntent = Intent(this, RotationListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

data class Message(val author: String, val body: String, val profileImage: String? = null)

@Composable
fun Conversation(navController: NavController, messages: List<Message>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(messages) { message ->
            MessageCard(msg = message)
        }
    }
}


