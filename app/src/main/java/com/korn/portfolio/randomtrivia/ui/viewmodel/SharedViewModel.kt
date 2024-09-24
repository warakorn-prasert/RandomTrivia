package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import java.util.Date

class SharedViewModel : ViewModel() {
    var onlineMode = false
    var settings = emptyList<GameSetting>()
    var game = Game(GameDetail(Date(), 0), emptyList())
}