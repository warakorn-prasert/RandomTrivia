package com.korn.portfolio.randomtrivia.ui

import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.model.Difficulty
import com.korn.portfolio.randomtrivia.model.Question

val mockCategory1 = Category("Category 1", false)
val mockCategory2 = Category("Category 2", true)

val mockQuestion11 = Question(
    question = "What is 1 + 1?",
    difficulty = Difficulty.EASY,
    categoryId = mockCategory1.id,
    correctAnswer = "2",
    incorrectAnswers = listOf("1", "3", "4")
)
val mockQuestion12 = Question(
    question = "Where are we?",
    difficulty = Difficulty.MEDIUM,
    categoryId = mockCategory1.id,
    correctAnswer = "Earth",
    incorrectAnswers = listOf("Moon", "Sun")
)
val mockQuestion21 = Question(
    question = "What are we?",
    difficulty = Difficulty.HARD,
    categoryId = mockCategory2.id,
    correctAnswer = "Humans",
    incorrectAnswers = listOf("Cats", "Dogs", "Fish")
)

val mockCategoryWithQuestions1 =
    CategoryWithQuestions(mockCategory1, listOf(mockQuestion11, mockQuestion12))
val mockCategoryWithQuestions2 =
    CategoryWithQuestions(mockCategory2, listOf(mockQuestion21))

val mockCategoryEmpty = CategoryWithQuestions(mockCategory1, emptyList())

private const val overflowText = "Overflow..............................................................."
val mockCategoryOverflowText = mockCategoryWithQuestions1
    .copy(category = mockCategory1.copy(name = overflowText))
