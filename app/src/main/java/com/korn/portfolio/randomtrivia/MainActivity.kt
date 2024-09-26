package com.korn.portfolio.randomtrivia

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.korn.portfolio.randomtrivia.ui.screen.MainScreen
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make edgeToEdge.detectDarkMode follows custom dark mode
        // and set system bar color
        val themeViewModel: ThemeViewModel by viewModels()
        lifecycleScope.launch {
            themeViewModel.getSystemBarData(this@MainActivity)
                .collect { (isDark, statusBarColor, navBarColor) ->
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            lightScrim = statusBarColor,
                            darkScrim = statusBarColor,
                            detectDarkMode = { _ -> isDark }
                        ),
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim = navBarColor,
                            darkScrim = navBarColor,
                            detectDarkMode = { _ -> isDark }
                        )
                    )
                    if (Build.VERSION.SDK_INT >= 29) {
                        window.statusBarColor = statusBarColor
                        window.navigationBarColor = navBarColor
                    }
                }
        }

        setContent {
            RandomTriviaTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen(Modifier.fillMaxSize())
                }
            }
        }
    }
}