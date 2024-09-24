package com.korn.portfolio.randomtrivia.ui.previewdata

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting

class GameSettingDataProvider : PreviewParameterProvider<GameSetting> {
    private val category = getCategory(0)
    override val values: Sequence<GameSetting> = sequenceOf(
        GameSetting(category, null, 50),
        GameSetting(category, Difficulty.EASY, 1),
        GameSetting(category, Difficulty.MEDIUM, 1),
        GameSetting(category, Difficulty.HARD, 1),
    )
}