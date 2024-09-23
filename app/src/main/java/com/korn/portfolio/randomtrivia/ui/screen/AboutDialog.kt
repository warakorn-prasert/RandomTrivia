@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("About this app", style = MaterialTheme.typography.titleMedium)
                    },
                    navigationIcon = {
                        IconButton({ onDismissRequest() }) {
                            Icon(Icons.Default.Close, "Close setting menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Credits(Modifier.padding(vertical = 8.dp))
                Spacer(Modifier.height(24.dp))
                AppDescription()
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LogoWithAppName()
                }
                LicenseIndication()
            }
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
private fun AppDescription() {
    val context = LocalContext.current
    Text("        " + context.resources.getString(R.string.about_app))
}

@Composable
private fun LogoWithAppName() {
    val color = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Random",
            color = color,
            style = MaterialTheme.typography.displayMedium
        )
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
            Text(
                text = "Trivia",
                color = color,
                style = MaterialTheme.typography.displayMedium
            )
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

@Preview
@Composable
fun AboutDialogPreview() {
    RandomTriviaTheme {
        AboutDialog(onDismissRequest = {})
    }
}