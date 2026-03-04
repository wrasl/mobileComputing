package com.example.myapp

import android.app.Activity
import android.content.Context
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapp.data.UserDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(navController: NavController, userDao: UserDao) {

    val context = LocalContext.current
    val activity = context as Activity

    // Load the user from Room
    val user = userDao.getUser()
    val currentUsername = user?.username ?: "Doge"
    val currentProfileImage = user?.imagePath

    // Update messages with current user info
    val updatedMessages = SampleData.conversationSample.map { msg ->
        msg.copy(author = currentUsername, profileImage = currentProfileImage)
    }

    Scaffold(
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
        }
    ) { paddingValues ->

        // Apply paddingValues to ensure the content is below the TopAppBar
        Conversation(
            navController = navController,
            messages = updatedMessages,
            modifier = Modifier.padding(paddingValues)
        )
    }
}