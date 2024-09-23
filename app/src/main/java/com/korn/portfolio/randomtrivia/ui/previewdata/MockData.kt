package com.korn.portfolio.randomtrivia.ui.previewdata

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import java.util.Date
import java.util.Random
import java.util.UUID

fun getCategory(id: Int) = Category(
    name = "Category name of id $id",
    downloadable = true,
    id = id
)

fun getQuestion(categoryId: Int, overflow: Boolean = false): Question {
    val id = UUID.randomUUID()
    return Question(
        question =
            if (!overflow) "Question statement of id ${id.toString().substring(0..5)}..."
            else "OverflowQuestionStatement".repeat(150),
        difficulty = Difficulty.entries.random(),
        categoryId = categoryId,
        correctAnswer = "Correct answer",
        incorrectAnswers = List(4) { "Incorrect answer ${it + 1}" },
        id = id
    )
}

fun getGameQuestion(
    category: Category,
    gameId: UUID = UUID.randomUUID(),
    overflow: Boolean = false,
    correctAnswer: Boolean = false
): GameQuestion {
    val question = getQuestion(category.id, overflow)
    return GameQuestion(
        question = question,
        answer = GameAnswer(
            gameId = gameId,
            questionId = question.id,
            answer =
                if (correctAnswer) "Correct answer"
                else (question.incorrectAnswers + "").random(),
            categoryId = category.id
        ),
        category = category
    )
}

fun getGame(totalQuestions: Int, overflowQuestionStatement: Boolean = false, played: Boolean = false): Game {
    val gameId = UUID.randomUUID()
    val gameQuestions = List(totalQuestions) {
        getGameQuestion(getCategory(it), gameId, overflowQuestionStatement, correctAnswer = if (played) Random().nextBoolean() else false)
    }
    return Game(
        detail = GameDetail(Date(), 0, gameId),
        questions = gameQuestions
    )
}