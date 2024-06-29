package com.korn.portfolio.randomtrivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.korn.portfolio.randomtrivia.ui.AppScreen
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RandomTriviaTheme(dynamicColor = true) {
                Surface(Modifier.fillMaxSize()) {
                    AppScreen()
                }
            }
        }
    }
}