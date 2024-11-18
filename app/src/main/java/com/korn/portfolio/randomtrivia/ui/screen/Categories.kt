@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
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

private enum class CategoryFilter(
    val displayText: String,
    val invoke: (List<CategoryDisplay>) -> List<CategoryDisplay>
) {
    ALL("All", { it }),
    PLAYED("Played", { all -> all.filter { it.isPlayed } }),
    NOT_PLAY("Not played", { all -> all.filter { !it.isPlayed } });

    companion object { val default = ALL }
}

private enum class CategorySort(
    val displayText: String,
    val invoke: (List<CategoryDisplay>) -> List<CategoryDisplay>
) {
    NAME("Name (A-Z)", { all -> all.sortedBy { it.name.lowercase() } }),
    TOTAL_QUESTIONS("Total questions (low-high)", { all -> all.sortedBy { it.totalQuestions } });

    companion object { val default = NAME }
}

private const val reverseSortDefault = false

@Composable
fun Categories(
    fetchStatus: FetchStatus,
    fetchCategories: () -> Unit,
    onCategoryClick: (categoryId: Int) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    val categories by viewModel.categories.collectAsState(emptyList())
    Categories(
        categories = categories,
        fetchStatus = fetchStatus,
        fetchCategories = fetchCategories,
        onCategoryClick = onCategoryClick,
        onAboutClick = onAboutClick,
        modifier = modifier
    )
}

@Composable
private fun Categories(
    categories: List<CategoryDisplay>,
    fetchStatus: FetchStatus,
    fetchCategories: () -> Unit,
    onCategoryClick: (categoryId: Int) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchWord by rememberSaveable { mutableStateOf("") }
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchableTopBar(
                searchWord = searchWord,
                onChange = { searchWord = it },
                hint = "Search for categories",
                onAboutClick = onAboutClick
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val listState = rememberLazyListState()
            var filter by rememberSaveable { mutableStateOf(CategoryFilter.default) }
            var sort by rememberSaveable { mutableStateOf(CategorySort.default) }
            var reverseSort by rememberSaveable { mutableStateOf(reverseSortDefault) }

            val filterSortCategories = categories
                .let { filter.invoke(it) }
                .filter { it.name.lowercase().contains(searchWord.lowercase())  }
                .let { sort.invoke(it) }
                .let { if (reverseSort) it.asReversed() else it }

            LaunchedEffect(searchWord, filter, reverseSort, sort) {
                listState.animateScrollToItem(0)
            }

            val itemsInView by remember {
                derivedStateOf {
                    listState.layoutInfo
                        .visibleItemsInfo.map { it.key as Int }
                }
            }

            CategoriesFilterSortMenuBar(
                filter, { filter = it },
                sort, { sort = it },
                reverseSort, { reverseSort = it }
            )

            FetchStatusBar(fetchStatus, fetchCategories)

            if (filterSortCategories.isEmpty() && fetchStatus != FetchStatus.Loading)
                Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), Alignment.Center) {
                    Text("No category available to play.")
                }

            val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomBarPadding = 80.dp
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = (imePadding - navBarPadding - bottomBarPadding + 8.dp).coerceAtLeast(8.dp)
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterSortCategories, key = { it.id }) { (name, total, totalPlayed, id, isPlayed) ->
                    val alpha by animateFloatAsState(
                        targetValue = if (id in itemsInView) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 50,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    Box(Modifier.alpha(alpha)) {
                        CategoryCard(
                            categoryName = name,
                            totalQuestions = total,
                            playedQuestions = totalPlayed,
                            isPlayed = isPlayed,
                            onClick = { onCategoryClick(id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesFilterSortMenuBar(
    filter: CategoryFilter,
    setFilter: (CategoryFilter) -> Unit,
    sort: CategorySort,
    setSort: (CategorySort) -> Unit,
    reverseSort: Boolean,
    setReverseSort: (Boolean) -> Unit
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = CategoryFilter.entries,
        onFilterSelect = setFilter,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort By")
                CheckboxWithText(
                    checked = reverseSort,
                    onCheckedChange = setReverseSort,
                    text = "Reversed"
                )
            }
            CategorySort.entries.forEach {
                RadioButtonWithText(
                    selected = sort == it,
                    onClick = { setSort(it) },
                    text = it.displayText
                )
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
        categories = List(5) {
            getCategory(it).run {
                CategoryDisplay(name, 10, it % 2, id)
            }
        },
        fetchStatus = FetchStatus.Success,
        fetchCategories = {},
        onCategoryClick = {},
        onAboutClick = {}
    )
}