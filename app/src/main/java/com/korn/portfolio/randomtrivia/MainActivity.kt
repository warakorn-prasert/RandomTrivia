package com.korn.portfolio.randomtrivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
            RandomTriviaTheme(
                themeViewModel.isDark,
                themeViewModel.sourceColor,
                themeViewModel.contrastLevel
            ) {
                Surface(Modifier.fillMaxSize()) {
                    Text("Hello World!")
                }
            }
        }
    }
}