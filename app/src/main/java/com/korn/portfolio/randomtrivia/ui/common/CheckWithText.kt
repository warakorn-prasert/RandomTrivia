@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxWithText(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .heightIn(min = 40.dp)
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onCheckedChange = null)
        Text(
            text = text,
            color = CheckboxDefaults.colors().run {
                if (checked) checkedBorderColor
                else uncheckedBorderColor
            },
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxWithTextOnPreview() {
    CheckboxWithText(true, {}, "Some text")
}

@Preview(showBackground = true)
@Composable
private fun CheckboxWithTextOffPreview() {
    CheckboxWithText(false, {}, "Some text")
}