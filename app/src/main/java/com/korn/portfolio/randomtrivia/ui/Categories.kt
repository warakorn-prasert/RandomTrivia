@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.model.ContrastLevel
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBar
import com.korn.portfolio.randomtrivia.ui.navigation.CategoriesNavigation
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun Categories() {
    val navController = rememberNavController()
    val categoriesViewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    NavHost(navController, startDestination = CategoriesNavigation.CATEGORIES.route) {
        composable(CategoriesNavigation.CATEGORIES.route) {
            Categories(categoriesViewModel)
        }
        composable(CategoriesNavigation.QUESTIONS.route) {
        }
    }
}

@Composable
private fun Categories(categoriesViewModel: CategoriesViewModel) {
    val searchWord by categoriesViewModel.searchWord.collectAsState()
    Scaffold(
        topBar = {
            SearchableTopBar(
                searchWord = searchWord,
                onChange = categoriesViewModel::setSearchWord,
                hint = "Search for categories"
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val categories by categoriesViewModel.categories.collectAsState(emptyList())
            val filter by categoriesViewModel.filter.collectAsState()
            val reverseSort by categoriesViewModel.reverseSort.collectAsState()
            val sort by categoriesViewModel.sort.collectAsState()

            val listState = rememberLazyListState()
            LaunchedEffect(searchWord, filter, reverseSort, sort) {
                listState.scrollToItem(0)
            }

            CategoriesFilterSortMenuBar(
                filter, categoriesViewModel::setFilter,
                sort, categoriesViewModel::setSort,
                reverseSort, categoriesViewModel::setReverseSort
            )
            FetchResultBar(
                categoriesViewModel.fetchStatus,
                categoriesViewModel::fetchCategories,
                Modifier.fillMaxWidth()
            )
            if (categories.isEmpty() && categoriesViewModel.fetchStatus != CategoryFetchStatus.Loading)
                Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), Alignment.Center) {
                    Text("No category available to play.")
                }
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { (name, total, isPlayed, _) ->
                    CategoryCard(
                        categoryName = name,
                        totalQuestion = total,
                        isPlayed = isPlayed,
                        onClick = {},
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
private fun FetchResultBar(
    fetchStatus: CategoryFetchStatus,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (fetchStatus !is CategoryFetchStatus.Success)
        Box(
            modifier = modifier
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (fetchStatus == CategoryFetchStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Loading new categories.",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else if (fetchStatus is CategoryFetchStatus.Error) {
                    Text(fetchStatus.message, style = MaterialTheme.typography.labelLarge)
                    IconButtonWithText(
                        onClick = retryAction,
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry button for fetching categories.",
                        text = "Retry"
                    )
                }
            }
        }
}

@Composable
private fun CategoryCard(
    categoryName: String,
    totalQuestion: Int,
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
                var detail = "$totalQuestion question"
                if (totalQuestion > 1) detail += "s"
                Text(text = detail)
            }
            if (isPlayed)
                IconButton(onClick) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Category card navigation button")
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryCardPreview() {
    val categoryName = "CategoryName"
    val totalQuestion = 100
    RandomTriviaTheme(contrastLevel = ContrastLevel.Custom(0f)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CategoryCard(
                categoryName = categoryName,
                totalQuestion = totalQuestion,
                isPlayed = false,
                onClick = {}
            )
            CategoryCard(
                categoryName = categoryName,
                totalQuestion = totalQuestion,
                isPlayed = true,
                onClick = {}
            )
            CategoryCard(
                categoryName = categoryName.repeat(10),
                totalQuestion = totalQuestion,
                isPlayed = true,
                onClick = {}
            )
        }
    }
}


@Preview
@Composable
private fun CategoriesPreview() {
    RandomTriviaTheme(contrastLevel = ContrastLevel.Custom(0f)) {
        Categories()
    }
}