@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import com.korn.portfolio.randomtrivia.ui.common.FetchStatusBar
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBar
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoriesViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoryDisplay
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoryFilter
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategorySort

@Composable
fun Categories(navToQuestions: (categoryId: Int) -> Unit) {
    val viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    val searchWord by viewModel.searchWord.collectAsState()

    val categories by viewModel.categories.collectAsState(emptyList())
    val filter by viewModel.filter.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val reverseSort by viewModel.reverseSort.collectAsState()
    Categories(
        searchWord = searchWord, setSearchWord = viewModel::setSearchWord,
        categories = categories,
        filter = filter, setFilter = viewModel::setFilter,
        sort = sort, setSort = viewModel::setSort,
        reverseSort = reverseSort, setReverseSort = viewModel::setReverseSort,
        fetchStatus = viewModel.fetchStatus, fetchCategories = viewModel::fetchCategories,
        navToQuestions = navToQuestions
    )
}

@Composable
private fun Categories(
    searchWord: String,
    setSearchWord: (String) -> Unit,

    categories: List<CategoryDisplay>,
    filter: CategoryFilter,
    sort: CategorySort,
    reverseSort: Boolean,
    setFilter: (CategoryFilter) -> Unit,
    setSort: (CategorySort) -> Unit,
    setReverseSort: (Boolean) -> Unit,

    fetchStatus: FetchStatus,
    fetchCategories: () -> Unit,

    navToQuestions: (categoryId: Int) -> Unit
) {
    Scaffold(
        topBar = {
            SearchableTopBar(
                searchWord = searchWord,
                onChange = setSearchWord,
                hint = "Search for categories"
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val listState = rememberLazyListState()
            LaunchedEffect(searchWord, filter, reverseSort, sort) {
                listState.scrollToItem(0)
            }

            CategoriesFilterSortMenuBar(
                filter, setFilter,
                sort, setSort,
                reverseSort, setReverseSort
            )
            FetchStatusBar(
                fetchStatus,
                fetchCategories
            )
            if (categories.isEmpty() && fetchStatus != FetchStatus.Loading)
                Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), Alignment.Center) {
                    Text("No category available to play.")
                }
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { (name, total, totalPlayed, id, isPlayed) ->
                    CategoryCard(
                        categoryName = name,
                        totalQuestions = total,
                        playedQuestions = totalPlayed,
                        isPlayed = isPlayed,
                        onClick = {
                            //viewModel.saveCategoryId(id)
                            navToQuestions(id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesFilterSortMenuBar(
    filter: CategoryFilter,
    onFilterSelect: (CategoryFilter) -> Unit,
    sort: CategorySort,
    onSortSelect: (CategorySort) -> Unit,
    reverseSort: Boolean,
    onReverseSortChange: (Boolean) -> Unit
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = CategoryFilter.entries,
        onFilterSelect = onFilterSelect,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Column(Modifier.height(IntrinsicSize.Min)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sort By")
                    CheckboxWithText(
                        checked = reverseSort,
                        onCheckedChange = onReverseSortChange,
                        text = "Reversed"
                    )
                }
                CategorySort.entries.forEach {
                    RadioButtonWithText(
                        selected = sort == it,
                        onClick = { onSortSelect(it) },
                        text = it.displayText
                    )
                }
            }
        }
    )
}

@Composable
private fun CategoryCard(
    categoryName: String,
    totalQuestions: Int,
    playedQuestions: Int,
    isPlayed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier
            .clip(CardDefaults.outlinedShape)
            .let {
                if (isPlayed) it.clickable { onClick() }
                else it
            }
    ) {
        Row(
            modifier = Modifier.padding(top = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = "Category card image",
                modifier = Modifier.size(56.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary, BlendMode.Color)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = categoryName, fontWeight = FontWeight.Bold)
                var detail = "$totalQuestions question"
                if (totalQuestions > 1) detail += "s"
                if (playedQuestions > 0) detail += " ($playedQuestions played)"
                Text(text = detail)
            }
            if (isPlayed)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Category card navigation button")
        }
    }
}

@Preview
@Composable
private fun CategoriesPreview() {
    Categories(
        searchWord = "", setSearchWord = {},
        categories = List(5) {
            getCategory(it).run {
                CategoryDisplay(name, 10, it % 2, id)
            }
        },
        filter = CategoryFilter.ALL, setFilter = {},
        sort = CategorySort.NAME, setSort = {},
        reverseSort = false, setReverseSort = {},
        fetchStatus = FetchStatus.Success, fetchCategories = {},
        navToQuestions = {}
    )
}