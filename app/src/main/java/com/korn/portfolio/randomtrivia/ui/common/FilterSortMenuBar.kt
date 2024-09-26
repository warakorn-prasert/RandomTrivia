@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

/**
 * Sort button to show bottom sheet
 * , followed by filter buttons initially selects the first item of collection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterSortMenuBar(
    selectedFilter: T,
    filters: Collection<T>,
    onFilterSelect: (T) -> Unit,
    filterToString: (T) -> String = { it.toString() },
    sortBottomSheetContent: @Composable ColumnScope.() -> Unit,

    /*
     Fixes bug :
        ThemeViewModel declared inside FilterSortMenuBar does not trigger
        flow when using ThemeViewModel.enableCustomNavBarColor.
        (Might be because of ModalBottomSheet)
     TODO : Change system bar color from inside FilterSortMenuBar
    */
    onShowSortMenuChange: (Boolean) -> Unit = {}
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var showSortMenu by remember { mutableStateOf(false) }

        LaunchedEffect(showSortMenu) {
            onShowSortMenuChange(showSortMenu)
        }

        IconChip(
            selected = false,
            onClick = { showSortMenu = true },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_sort),
                    contentDescription = "Sort menu icon"
                )
            }
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach {
                TextChip(
                    selected = filterToString(it) == filterToString(selectedFilter),
                    onClick = {
                        onFilterSelect(it)
                    },
                    text = filterToString(it)
                )
            }
        }
        if (showSortMenu) {
            ModalBottomSheet(
                onDismissRequest = { showSortMenu = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    sortBottomSheetContent()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterSortMenuBarPreview() {
    RandomTriviaTheme {
        FilterSortMenuBar(
            selectedFilter = "T1",
            filters = (1..10).map { "T$it" },
            onFilterSelect = {},
            sortBottomSheetContent = { Text("Sort content") }
        )
    }
}