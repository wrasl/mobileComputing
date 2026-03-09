package com.example.myapp

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapp.data.MessageDao
import com.example.myapp.data.MessageEntity
import com.example.myapp.data.UserDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(navController: NavController, userDao: UserDao, messageDao: MessageDao) {

    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    // Load the user from Room
    val user = userDao.getUser()
    val currentUsername = user?.username ?: "Doge"
    val currentProfileImage = user?.imagePath

    // Observe messages from the database
    val messageEntities by messageDao.getAllMessages().collectAsState(initial = emptyList())
    
    // Map MessageEntity to Message, including the new imagePath field
    val messages = messageEntities.map { entity ->
        Message(
            author = entity.author,
            body = entity.body,
            profileImage = entity.profileImage,
            imagePath = entity.imagePath
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("details/$currentUsername")
                    })
                    {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        // Close the app by finishing the activity
                        activity.finish()
                    })
                    {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = Color.White
                )
            )
        },

        bottomBar = {
            MessageInput(
                navController = navController,
                onSend = { text ->
                    scope.launch {
                        messageDao.insertMessage(
                            MessageEntity(
                                author = currentUsername,
                                body = text,
                                profileImage = currentProfileImage
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // Apply paddingValues to ensure the content is below the TopAppBar
        Conversation(
            navController = navController,
            messages = messages,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
