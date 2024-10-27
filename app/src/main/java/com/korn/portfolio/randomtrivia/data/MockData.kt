package com.korn.portfolio.randomtrivia.data

import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import java.util.Date
import java.util.UUID

val mockCategory1: Category
    get() = Category("Category 1", 1)
val mockCategory2: Category
    get() = Category("Category 2", 2)

val mockQuestion11: Question
    get() = Question(
        question = "What is 1 + 1?",
        difficulty = Difficulty.EASY,
        categoryId = mockCategory1.id,
        correctAnswer = "2",
        incorrectAnswers = listOf("1", "3", "4")
    )
val mockQuestion12: Question
    get() = Question(
        question = "Where are we?",
        difficulty = Difficulty.MEDIUM,
        categoryId = mockCategory1.id,
        correctAnswer = "Earth",
        incorrectAnswers = listOf("Moon", "Sun")
    )
val mockQuestion21: Question
    get() = Question(
        question = "What are we?",
        difficulty = Difficulty.HARD,
        categoryId = mockCategory2.id,
        correctAnswer = "Humans",
        incorrectAnswers = listOf("Cats", "Dogs", "Fish")
    )

val mockCategoryWithQuestions1: CategoryWithQuestions
    get() = CategoryWithQuestions(
        category = mockCategory1,
        questions = listOf(mockQuestion11, mockQuestion12)
    )
val mockCategoryWithQuestions2: CategoryWithQuestions
    get() = CategoryWithQuestions(
        category = mockCategory2,
        questions = listOf(mockQuestion21)
    )

val mockCategoryEmpty: CategoryWithQuestions
    get() = CategoryWithQuestions(
        category = mockCategory1,
        questions = emptyList()
    )

private const val overflowText = "Overflow..............................................................."
val mockCategoryOverflowText: CategoryWithQuestions
    get() = mockCategoryWithQuestions1.copy(
        category = mockCategory1.copy(
            name = overflowText
        )
    )

private fun Question.toGameAnswer(gameId: UUID) =
    GameAnswer(
        gameId = gameId,
        questionId = id,
        answer = "Answer of $question",
        categoryId = categoryId
    )

val mockGameDetail: GameDetail
    get() = GameDetail(
        timestamp = Date(),
        totalTimeSecond = 1234
    )

data class MockData(
    val category1: Category = mockCategory1,
    val category2: Category = mockCategory2,
) {
    val question11: Question = mockQuestion11.copy(categoryId = category1.id)
    val question12: Question = mockQuestion12.copy(categoryId = category1.id)
    val question21: Question = mockQuestion21.copy(categoryId = category2.id)

    val categoriesWithQuestions: List<CategoryWithQuestions> = listOf(
        CategoryWithQuestions(
            category1,
            listOf(question11, question12)
        ),
        CategoryWithQuestions(category2, listOf(question21))
    )

    val gameDetail: GameDetail = mockGameDetail
    val qameAnswer11: GameAnswer = question11.toGameAnswer(gameDetail.gameId)
    val qameAnswer12: GameAnswer = question12.toGameAnswer(gameDetail.gameId)
    val qameAnswer21: GameAnswer = question21.toGameAnswer(gameDetail.gameId)

    val gameQuestion11 =
        GameQuestion(question11, qameAnswer11, category1)
    val gameQuestion12 =
        GameQuestion(question12, qameAnswer12, category1)
    val gameQuestion21 =
        GameQuestion(question21, qameAnswer21, category2)
    val gameQuestions: List<GameQuestion> = listOf(gameQuestion11, gameQuestion12, gameQuestion21)
}

val mockGame: Game = with(MockData()) {
    Game(
        detail = gameDetail,
        questions = gameQuestions
    )
}