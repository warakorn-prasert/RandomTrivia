package com.korn.portfolio.randomtrivia.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.model.QuestionAnswer

@Composable
fun MyTextField(
    text: MutableState<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text.value,
        onValueChange = { text.value = it },
        modifier = modifier.focusRequester(focusRequester),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true
    )
}

@Composable
fun AlertCard(
    dismissAction: () -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column {
            IconButton(dismissAction, Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Close, null)
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                title()
                HorizontalDivider(Modifier.padding(12.dp), 2.dp)
                content()
            }

        }
    }
}

@Composable
fun QuestionCard(
    label: String,
    questionAnswer: QuestionAnswer,
    enabled: Boolean = true,
    showResult: Boolean = false,
    modifier: Modifier = Modifier
) {
    val options: List<String> = (questionAnswer.question.incorrectAnswers
            + questionAnswer.question.correctAnswer).sortedBy { it }
    var answer by remember { mutableStateOf(questionAnswer.answer) }

    Card(modifier) {
        Column {
            Text(label, Modifier.align(Alignment.CenterHorizontally))
            Text(questionAnswer.question.question)
            options.forEach { option ->
                val color = when (option) {
                    questionAnswer.question.correctAnswer -> Color.Green
                    answer -> Color.Red
                    else -> Color.Unspecified
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = answer == option,
                        onCheckedChange = {
                            answer = option
                            questionAnswer.answer = answer
                        },
                        enabled = enabled,
                        colors = if (!showResult) {
                            CheckboxDefaults.colors()
                        } else {
                            CheckboxDefaults.colors().copy(
                                checkedBorderColor = color,
                                uncheckedBorderColor = color,
                                disabledBorderColor = color,
                                disabledUncheckedBorderColor = color
                            )
                        }
                    )
                    Text(option)
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
private fun MyTextFieldPreview() {
    Column {
        MyTextField(mutableStateOf(""), "Label", Modifier)
        MyTextField(mutableStateOf("Some value"), "Label", Modifier)
    }
}

@Preview
@Composable
private fun AlertCardPreview() {
    val sampleText = "This is a short content."
    AlertCard(
        dismissAction = {},
        title = { Text(sampleText.repeat(10)) },
        content = { Text(sampleText.repeat(10)) }
    )
}