@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.previewdata.PreviewWindowSizes
import com.korn.portfolio.randomtrivia.ui.previewdata.windowSizeForPreview
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    BackHandler { onExit() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("About this app", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onExit) {
                        Icon(Icons.Default.Close, "Close setting menu")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Credits(Modifier.padding(vertical = 8.dp))
            if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppDescription(Modifier.padding(24.dp).weight(1f))
                    LogoWithAppName(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(24.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(24.dp))
                AppDescription()
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LogoWithAppName()
                }
            }
            Spacer(Modifier.height(24.dp))
            LicenseIndication()
        }
    }
}

@Composable
private fun Credits(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val context = LocalContext.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CreditText("Trivia By")
            val uriHandler = LocalUriHandler.current
            val website = "https://" + context.resources.getString(R.string.trivia_by_website)
            CreditText(
                text = "OPENTDB.COM",
                modifier = Modifier.clickable { uriHandler.openUri(website) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CreditText("Developed By")
            CreditText(context.resources.getString(R.string.developer_name))
        }
    }
}

@Composable
private fun CreditText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AppDescription(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Text(
        text = "        " + context.resources.getString(R.string.about_app),
        modifier = modifier
    )
}

@Composable
private fun LogoWithAppName(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.displayMedium) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = "Random", color = color)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_android),
                    contentDescription = "App icon",
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    tint = color
                )
                Text(text = "Trivia", color = color)
            }
        }
    }
}

@Composable
private fun ColumnScope.LicenseIndication() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    Text(
        text = context.resources.getString(R.string.trivia_by_license),
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = 16.dp)
            .clickable { uriHandler.openUri(context.resources.getString(R.string.trivia_by_license_uri)) },
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
    )
}

@PreviewWindowSizes
@Composable
private fun AboutPreview() {
    RandomTriviaTheme {
        AboutScreen({}, windowSizeClass = windowSizeForPreview())
    }
}