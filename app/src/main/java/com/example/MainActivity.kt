package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MainScreen
import com.example.ui.PlannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = PlannerViewModel.Factory(application)
        val viewModel = ViewModelProvider(this, factory)[PlannerViewModel::class.java]

        setContent {
            MainScreen(viewModel = viewModel)
        }
    }
}
