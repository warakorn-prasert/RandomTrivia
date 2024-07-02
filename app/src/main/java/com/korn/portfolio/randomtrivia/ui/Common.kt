@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import kotlin.math.roundToInt

// If no internet, loading animation will end too soon.
const val fakeLoadingTimeMillis = 500L

val horizontalPadding = 12.dp

val QuestionCount.invalid: Boolean
    get() = total != (easy + medium + hard)

val Game.score: Pair<Int, Int>
    get() = questions
        .fold(0 to 0) { acc, (question, answer, _) ->
            val correct = question.correctAnswer == answer.answer
            acc.copy(
                first = acc.first + if (correct) 1 else 0,
                second = acc.second + 1
            )
        }

@Composable
fun <T> DropdownButton(
    selection: T,
    onSelected: (T) -> Unit,
    items: Collection<T>,
    toString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(Modifier.clickable { expanded = !expanded }) {
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(toString(selection), Modifier.weight(1f))
            Icon(Icons.Default.run { if (expanded) KeyboardArrowUp else KeyboardArrowDown }, null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(toString(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SliderMenu(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int
) {
    Row {
        IconButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
            enabled = value > minValue
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
        }
        Column(Modifier.weight(1f)) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                valueRange = minValue.toFloat()..maxValue.toFloat(),
                steps = maxValue - minValue
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(minValue.toString())
                Text(value.toString())
                Text(maxValue.toString())
            }
        }
        IconButton(
            onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
            enabled = value < maxValue
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}