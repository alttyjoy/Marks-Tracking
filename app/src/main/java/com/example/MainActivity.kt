package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.AppNavigationShell
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MarksViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MarksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode = viewModel.themeMode
            val darkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = darkTheme) {
                AppNavigationShell(viewModel = viewModel)
            }
        }
    }
}
