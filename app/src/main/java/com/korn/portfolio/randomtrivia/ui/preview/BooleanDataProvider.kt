package com.korn.portfolio.randomtrivia.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanDataProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(false, true)
}