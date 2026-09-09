package com.schemalens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.schemalens.app.ui.MainScreen
import com.schemalens.app.ui.theme.SchemaLensTheme
import com.schemalens.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchemaLensTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
