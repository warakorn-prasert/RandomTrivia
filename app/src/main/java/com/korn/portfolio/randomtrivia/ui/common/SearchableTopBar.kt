@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.screen.AboutDialog
import com.korn.portfolio.randomtrivia.ui.screen.SettingDialog
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun SearchableTopBar(
    searchWord: String,
    onChange: (String) -> Unit,
    hint: String = "",
    title: String = "Random Trivia",
    hideSearchButton: Boolean = false
) {
    SearchableTopBarDefault(searchWord, onChange, hint, title, hideSearchButton = hideSearchButton)
}

@Composable
fun SearchableTopBarWithBackButton(
    searchWord: String,
    onChange: (String) -> Unit,
    hint: String = "",
    title: String = "Random Trivia",
    onBackButtonClick: () -> Unit
) {
    SearchableTopBarDefault(searchWord, onChange, hint, title, onBackButtonClick, false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableTopBarDefault(
    searchWord: String,
    onChange: (String) -> Unit,
    hint: String = "",
    title: String = "Random Trivia",
    onNavigateBackClick: (() -> Unit)? = null,  // If not null, icon will be app logo.
    hideSearchButton: Boolean = false
) {
    var searching by remember { mutableStateOf(false) }

    LaunchedEffect(searching) {
        if (searchWord.isBlank()) onChange("")
    }

    TopAppBar(
        title = when {
            !searching && searchWord.isBlank() -> {{
                Text(title, style = MaterialTheme.typography.titleMedium)
            }}
            !searching && searchWord.isNotBlank() -> {{
                Text(searchWord, style = MaterialTheme.typography.bodyMedium)
            }}
            else -> {{
                val focusRequester = remember { FocusRequester() }
                val textState = remember { mutableStateOf(TextFieldValue(searchWord)) }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    // move cursor position to last
                    textState.value = textState.value.copy(
                        selection = TextRange(textState.value.text.length)
                    )
                }
                BasicTextField(
                    value = textState.value,
                    onValueChange = {
                        textState.value = it
                        onChange(it.text)
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { searching = false }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                ) { innerTextField ->
                    Box {
                        if (searchWord.isEmpty())
                            Text(hint, style = MaterialTheme.typography.bodyMedium)
                        innerTextField()
                    }
                }
            }}
        },
        navigationIcon = {
            if (!searching && searchWord.isBlank() && onNavigateBackClick == null)
                Icon(
                    painter = painterResource(R.drawable.ic_android),
                    contentDescription = "App icon",
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            if (onNavigateBackClick != null)
                IconButton(onNavigateBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Button to navigate back")
                }
        },
        actions = {
            if (!searching) {
                if (!hideSearchButton)
                    IconButton({ searching = true }) {
                        Icon(Icons.Default.Search, "Search button")
                    }
                Column {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton({ menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, "Button to open mini menu")
                    }
                    var showSetting by remember { mutableStateOf(false) }
                    var showAbout by remember { mutableStateOf(false) }
                    LaunchedEffect(showSetting, showAbout) {
                        menuExpanded = false
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Setting") },
                            onClick = { showSetting = true }
                        )
                        DropdownMenuItem(
                            text = { Text("About App") },
                            onClick = { showAbout = true }
                        )
                    }
                    if (showSetting)
                        SettingDialog(onDismissRequest = { showSetting = false })
                    if (showAbout)
                        AboutDialog(onDismissRequest = { showAbout = false })
                }
            } else {
                IconButton({ searching = false }) {
                    Icon(Icons.Default.Check, "Done searching")
                }
                IconButton({
                    onChange("")
                    searching = false
                }) {
                    Icon(Icons.Default.Close, "Cancel searching")
                }
            }
        }
    )
}

@Preview
@Composable
private fun TopBarSearchPreview() {
    RandomTriviaTheme {
        var searchWord by remember { mutableStateOf("") }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchableTopBar(
                searchWord = searchWord,
                onChange = { searchWord = it },
                hint = "Search for ...",
                title = "Main top bar"
            )
            SearchableTopBarWithBackButton(
                searchWord = searchWord,
                onChange = { searchWord = it },
                hint = "Search for ...",
                title = "Backable top bar",
                onBackButtonClick = {}
            )
        }
    }
}