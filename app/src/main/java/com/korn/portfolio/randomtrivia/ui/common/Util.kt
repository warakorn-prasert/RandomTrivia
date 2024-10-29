package com.korn.portfolio.randomtrivia.ui.common

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category

fun hhmmssFrom(second: Int): String {
    fun format(value: Int): String = if (value < 10) "0$value" else value.toString()
    val ss = format(second % 60)
    val mm = format((second / 60) % 60)
    val hh = format(second / 3600)
    return if (hh.toInt() < 100) "$hh:$mm:$ss" else ">100 hrs"
}

val Category?.displayName: String
    get() = this?.name ?: "Random"

val Difficulty?.displayName: String
    get() = when (this) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
        null -> "Random"
    }