package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.MyAppTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.room.Room
import com.example.myapp.data.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {

                val context = this

                val db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_db"
                )
                    .allowMainThreadQueries()
                    .build()

                val userDao = db.userDao()

                // Ensure a default user exists
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


