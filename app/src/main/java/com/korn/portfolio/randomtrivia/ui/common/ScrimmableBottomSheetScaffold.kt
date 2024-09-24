@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrimmableBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.(PaddingValues) -> Unit,
    sheetContentPeekHeight: Dp,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    /**
     * Fix : Bottom sheet is fully hidable after screen orientation. (Sol : Don't use rememberSaveable.)
     * @see androidx.compose.material3.rememberSheetState
     */
    val scaffoldSheetState = rememberBottomSheetScaffoldState(
        SheetState(
            skipPartiallyExpanded = false,
            density = LocalDensity.current,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )
    val sheetPeekHeight = 48.dp /* (handle) */ + sheetContentPeekHeight
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    var showScrim by remember { mutableStateOf(false) }
    BottomSheetScaffold(
        sheetContent = {
            sheetContent(PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp))
        },
        scaffoldState = scaffoldSheetState,
        sheetPeekHeight = sheetPeekHeight,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                Modifier.onGloballyPositioned {
                    val sheetPeekHeightPx = with(density) { sheetPeekHeight.toPx() }
                    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                    val bottomSheetHeight = screenHeightPx - it.positionInWindow().y
                    showScrim = bottomSheetHeight > sheetPeekHeightPx
                }
            )
        },
        topBar = topBar?.let {{
            BottomSheetScrimmableContent(
                sheetState = scaffoldSheetState.bottomSheetState,
                showScrim = showScrim
            ) {
                it.invoke()
            }
        }}
    ) { paddingValues ->
        BottomSheetScrimmableContent(
            sheetState = scaffoldSheetState.bottomSheetState,
            showScrim = showScrim
        ) {
            content(paddingValues)
        }
    }
}

/** @see androidx.compose.material3.Scrim */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("AnimateAsStateLabel")
@Composable
private fun BottomSheetScrimmableContent(
    sheetState: SheetState,
    // Use explicit showScrim instead of sheetState.value == SheetValue.Expanded
    // to show scrim immediately when dragging, not after finishing.
    showScrim: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    fun onDismissRequest() {
        scope.launch { sheetState.partialExpand() }
    }

    val color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    val alpha by animateFloatAsState(
        targetValue = if (showScrim) 1f else 0f,
        animationSpec = TweenSpec()
    )

    val dismissSheet = if (showScrim) {
        Modifier
            .pointerInput(::onDismissRequest) {
                detectTapGestures {
                    onDismissRequest()
                }
            }
            .clearAndSetSemantics {}
    } else {
        Modifier
    }
    var w by remember { mutableIntStateOf(0) }
    var h by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(
        Modifier.onSizeChanged {
            w = it.width
            h = it.height
        }
    ) {
        content()
        Canvas(
            Modifier
                // For implementation flexibility, fillMaxSize and intrinsic sizes are not used.
                .size(
                    width = with(density) { w.toDp() },
                    height = with(density) { h.toDp() }
                )
                .then(dismissSheet)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}

@Preview
@Composable
private fun ScrimmableBottomSheetScaffoldPreview() {
    ScrimmableBottomSheetScaffold(
        sheetContent = { Text("Bottom sheet content") },
        sheetContentPeekHeight = 96.dp,
        content = { Text("Content") }
    )
}