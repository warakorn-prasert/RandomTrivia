package com.korn.portfolio.randomtrivia

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.korn.portfolio.randomtrivia.ui.screen.MainScreen
import com.korn.portfolio.randomtrivia.ui.screen.Splash
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.MainViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels(factoryProducer = { MainViewModel.Factory })

        // Enable edgeToEdge and make detectDarkMode follows custom dark mode
        val themeViewModel: ThemeViewModel by viewModels()
        lifecycleScope.launch {
            themeViewModel.getIsDark(this@MainActivity).collect { isDark ->
                val systemBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                    detectDarkMode = { _ ->
                        themeViewModel.getIsDarkValue(this@MainActivity, isDark)
                    }
                )
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle
                )
            }
        }

        setContent {
            RandomTriviaTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    AnimatedVisibility(mainViewModel.showSplashScreen) {
                        Splash(
                            onDone = {
                                mainViewModel.showSplashScreen = false
                            }
                        )
                    }
                }
            }
        }
    }
}