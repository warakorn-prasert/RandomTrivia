@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.model.ContrastLevel
import com.korn.portfolio.randomtrivia.ui.common.FullScreenDialog
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun AboutDialog(expanded: Boolean, onDismissRequest: () -> Unit) {
    FullScreenDialog(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        title = "About this app"
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Credits(Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.height(24.dp))
            AppDescription()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LogoWithAppName()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CreditText("Trivia By")
            CreditText("OPENTDB.COM")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CreditText("Developed By")
            CreditText("KORN")
        }
    }
}

@Composable
private fun CreditText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AppDescription() {
    Text("    Random Trivia ...")
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

@Preview
@Composable
fun AboutDialogPreview() {
    RandomTriviaTheme(contrastLevel = ContrastLevel.Custom(0f)) {
        AboutDialog(
            expanded = true,
            onDismissRequest = {}
        )
    }
}