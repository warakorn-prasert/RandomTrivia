@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.ui.common.CategoryParceler
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import com.korn.portfolio.randomtrivia.ui.common.QuestionParceler
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoryDisplay
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

// Can't use original CategoryWithQuestions because it's not parcelable.
@Parcelize
private data class ParcelableCategoryWithQuestions(
    val category: @WriteWith<CategoryParceler> Category,
    val question: List< @WriteWith<QuestionParceler> Question>
) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CategoriesAndQuestions(
    categories: List<CategoryDisplay>,
    categoriesFetchStatus: FetchStatus,
    onRetryFetch: () -> Unit,
    onAboutClick: () -> Unit,
    onGetQuestionsRequest: (categoryId: Int, onDone: (List<Question>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ParcelableCategoryWithQuestions>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {},
        floatingActionButton = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    Categories(
                        categories = categories,
                        categoriesFetchStatus = categoriesFetchStatus,
                        onRetryFetch = onRetryFetch,
                        onCategoryClick = { category ->
                            onGetQuestionsRequest(category.id) { questions ->
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, ParcelableCategoryWithQuestions(category, questions))
                            }
                        },
                        onAboutClick = onAboutClick
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.content?.let { (category, questions) ->
                        Questions(
                            categoryName = category.name,
                            questions = questions,
                            onExit = {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                            },
                            onAboutClick = onAboutClick
                        )
                    }
                }
            },
            modifier = modifier.padding(paddingValues)
        )
    }
}