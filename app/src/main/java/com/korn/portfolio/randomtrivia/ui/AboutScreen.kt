package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.korn.portfolio.randomtrivia.R

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DEVELOPED BY")
            Text(stringResource(R.string.developer_full_name))
            Text(stringResource(R.string.developer_github))
            Text(stringResource(R.string.developer_linkedin))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TRIVIA BY")
            Text(stringResource(R.string.trivia_by_company))
            Text(stringResource(R.string.trivia_by_website))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    AboutScreen()
}