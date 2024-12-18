package com.korn.portfolio.randomtrivia

import android.graphics.Color
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.korn.portfolio.randomtrivia.ui.screen.MainScreen
import com.korn.portfolio.randomtrivia.ui.theme.M3Purple
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.theme.TriviaAppColor
import com.korn.portfolio.randomtrivia.ui.viewmodel.dynamicColorScheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun create_view_model() {
        composeTestRule.activity.setContent {
            RandomTriviaTheme {
                MainScreen()
            }
        }
    }

    @Test
    fun navigate_bottom_bar() {
        // Categories screen
        composeTestRule.onNodeWithText("All").assertExists()
        composeTestRule.onNodeWithText("Played").assertExists()
        composeTestRule.onNodeWithText("Not played").assertExists()
        // Play screen
        composeTestRule.onNodeWithText("Play").performClick()
        composeTestRule.onNodeWithText("Start game").assertExists()
        // History screen
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.onNodeWithText("All").assertExists()
        composeTestRule.onNodeWithText("Today").assertExists()
        // Back press always goes to Categories screen
        Espresso.pressBack()
        composeTestRule.onNodeWithText("All").assertExists()
        composeTestRule.onNodeWithText("Played").assertExists()
        composeTestRule.onNodeWithText("Not played").assertExists()
    }

    @Test
    fun open_about_screen() {
        composeTestRule.onNodeWithContentDescription("Button to open mini menu").performClick()
        composeTestRule.onNodeWithText("About App").performClick()
        composeTestRule.onNodeWithText("About this app").assertExists()
    }

    @Test
    fun change_dark_theme() {
        val buffer = IntArray(1)
        // start with light theme
        openSetting()
        composeTestRule.onNodeWithText("Dark Theme").performClick()
        composeTestRule.onAllNodesWithText("Light")
            .onLast()  // in-case light is already chosen
            .performClick()
        closeSetting()
        // get light color
        composeTestRule.onRoot()
            .captureToImage()
            .readPixels(buffer, width = 1, height = 1)
        val topBarLight = FloatArray(3).let {
            Color.colorToHSV(buffer[0], it)
            it[2]
        }
        // change to dark theme
        openSetting()
        composeTestRule.onNodeWithText("Dark Theme").performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        closeSetting()
        // get dark color
        composeTestRule.onRoot().captureToImage()
            .readPixels(buffer, width = 1, height = 1)
        val topBarDark = FloatArray(3).let {
            Color.colorToHSV(buffer[0], it)
            it[2]
        }
        assert(topBarLight > topBarDark)
    }

    @Test
    fun change_theme_color() {
        val buffer = IntArray(1)
        val expectedTopBar1 = dynamicColorScheme(TriviaAppColor, false, 0.0)
            .surface
            .toArgb()
        val expectedTopBar2 = dynamicColorScheme(M3Purple, false, 0.0)
            .surface
            .toArgb()
        // start with app color and light theme
        openSetting()
        composeTestRule.onNodeWithText("Dark Theme").performClick()
        composeTestRule.onNodeWithText("Light").performClick()
        composeTestRule.onNodeWithText("Theme color").performClick()
        composeTestRule.onAllNodesWithText("App color").onFirst().performClick()
        closeSetting()
        // get color
        composeTestRule.onRoot().captureToImage()
            .readPixels(buffer, width = 1, height = 1)
        val topBar1 = buffer[0]

        assertEquals(expectedTopBar1, topBar1)
        // change to preset 1
        openSetting()
        composeTestRule.onNodeWithText("Theme color").performClick()
        composeTestRule.onAllNodesWithText("Preset 1").onFirst().performClick()
        closeSetting()
        // get color
        composeTestRule.onRoot().captureToImage()
            .readPixels(buffer, width = 1, height = 1)
        val topBar2 = buffer[0]
        assertEquals(expectedTopBar2, topBar2)
    }

    private fun openSetting() {
        composeTestRule.onNodeWithContentDescription("Button to open mini menu").performClick()
        composeTestRule.onNodeWithText("Setting").performClick()
    }

    private fun closeSetting() {
        composeTestRule.onNodeWithContentDescription("Close setting menu").performClick()
    }
}