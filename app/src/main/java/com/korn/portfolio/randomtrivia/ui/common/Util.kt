package com.korn.portfolio.randomtrivia.ui.common

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.ui.navigation.WrappedGame
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import java.util.Date
import java.util.UUID

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

// Ref. : https://stackoverflow.com/a/65272372
@OptIn(ExperimentalSerializationApi::class)
object GameSettingSerializer : KSerializer<GameSetting> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GameSetting") {
        element<String?>("categoryName")
        element<Int?>("categoryId")
        element<String?>("difficulty")
        element<Int>("amount")
    }

    override fun deserialize(decoder: Decoder): GameSetting {
        return decoder.decodeStructure(descriptor) {
            var categoryName: String? = null
            var categoryId: Int? = null
            var difficulty: Difficulty? = null
            var amount = 0

            var decodeCount = 0
            loop@ while(true) {
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop
                    0 -> categoryName =
                        decodeNullableSerializableElement(descriptor, 0, String.serializer().nullable)
                    1 -> categoryId =
                        decodeNullableSerializableElement(descriptor, 1, Int.serializer().nullable)
                    2 -> difficulty =
                        decodeNullableSerializableElement(descriptor, 2, String.serializer().nullable)
                            ?.let { Difficulty.valueOf(it) }
                    3 -> amount = decodeIntElement(descriptor, 3)
                    else -> throw SerializationException("Unexpected index $index")
                }
                decodeCount++
            }

            require(decodeCount == descriptor.elementsCount)

            GameSetting(
                category =
                    if (categoryName == null || categoryId == null) null
                    else Category(categoryName, categoryId),
                difficulty  = difficulty,
                amount = amount
            )
        }
    }

    override fun serialize(encoder: Encoder, value: GameSetting) {
        encoder.encodeStructure(descriptor) {
            encodeNullableSerializableElement(descriptor, 0, String.serializer().nullable, value.category?.name)
            encodeNullableSerializableElement(descriptor, 1, Int.serializer().nullable, value.category?.id)
            encodeNullableSerializableElement(descriptor, 2, String.serializer().nullable, value.difficulty?.toString())
            encodeIntElement(descriptor, 3, value.amount)
        }
    }
}

// Ref. : https://stackoverflow.com/a/65272372
@OptIn(ExperimentalSerializationApi::class)
object GameSerializer : KSerializer<Game> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Game") {
        // game detail
        element<Long>("timestamp")
        element<Int>("totalTimeSecond")
        element<String>("gameId")
        // question categories
        element<List<String?>>("categoryNames")
        element<List<Int?>>("categoryIds")
        // question answers
        element<List<String>>("answers")
        // question
        element<List<String>>("questions")
        element<List<String>>("difficulties")
        element<List<String>>("correctAnswers")
        element<List<List<String>>>("incorrectAnswers")
        element<List<String>>("questionIds")
    }

    override fun deserialize(decoder: Decoder): Game {
        return decoder.decodeStructure(descriptor) {
            var timestamp = Date()
            var totalTimeSecond = 0
            var gameId = UUID.randomUUID()

            var categoryNames = emptyList<String?>()
            var categoryIds = emptyList<Int?>()
            var answers = emptyList<String>()
            var questions = emptyList<String>()
            var difficulties = emptyList<Difficulty>()
            var correctAnswers = emptyList<String>()
            var incorrectAnswers = emptyList<List<String>>()
            var questionIds = emptyList<UUID>()

            var decodeCount = 0
            loop@ while(true) {
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop
                    0 -> timestamp = decodeLongElement(descriptor, 0).let { Date(it) }
                    1 -> totalTimeSecond = decodeIntElement(descriptor, 1)
                    2 -> gameId = decodeStringElement(descriptor, 2).let { UUID.fromString(it) }
                    3 -> categoryNames =
                        decodeSerializableElement(descriptor, 3, ListSerializer(String.serializer().nullable))
                    4 -> categoryIds =
                        decodeSerializableElement(descriptor, 4, ListSerializer(Int.serializer().nullable))
                    5 -> answers =
                        decodeSerializableElement(descriptor, 5, ListSerializer(String.serializer()))
                    6 -> questions =
                        decodeSerializableElement(descriptor, 6, ListSerializer(String.serializer()))
                    7 -> difficulties =
                        decodeSerializableElement(descriptor, 7, ListSerializer(String.serializer()))
                            .map { Difficulty.valueOf(it) }
                    8 -> correctAnswers =
                        decodeSerializableElement(descriptor, 8, ListSerializer(String.serializer()))
                    9 -> incorrectAnswers =
                        decodeSerializableElement(descriptor, 9, ListSerializer(ListSerializer(String.serializer())))
                    10 -> questionIds =
                        decodeSerializableElement(descriptor, 10, ListSerializer(String.serializer()))
                            .map { UUID.fromString(it) }
                    else -> throw SerializationException("Unexpected index $index")
                }
                decodeCount++
            }

            require(decodeCount == descriptor.elementsCount)
            require(categoryNames.size.let { refSize ->
                refSize == categoryIds.size
                        && refSize == answers.size
                        && refSize == questions.size
                        && refSize == difficulties.size
                        && refSize == correctAnswers.size
                        && refSize == incorrectAnswers.size
                        && refSize == questionIds.size
            })

            Game(
                detail = GameDetail(
                    timestamp = timestamp,
                    totalTimeSecond = totalTimeSecond,
                    gameId = gameId
                ),
                questions = categoryNames.mapIndexed { idx, categoryName ->
                    GameQuestion(
                        question = Question(
                            question = questions[idx],
                            difficulty = difficulties[idx],
                            categoryId = categoryIds[idx],
                            correctAnswer = correctAnswers[idx],
                            incorrectAnswers = incorrectAnswers[idx],
                            id = questionIds[idx]
                        ),
                        answer = GameAnswer(
                            gameId = gameId,
                            questionId = questionIds[idx],
                            answer = answers[idx],
                            categoryId = categoryIds[idx]
                        ),
                        category = run {
                            val categoryId = categoryIds[idx]
                            if (categoryName == null || categoryId == null) null
                            else Category(name = categoryName, id = categoryId)
                        }
                    )
                }
            )
        }
    }

    override fun serialize(encoder: Encoder, value: Game) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.detail.timestamp.time)
            encodeIntElement(descriptor, 1, value.detail.totalTimeSecond)
            encodeStringElement(descriptor, 2, value.detail.gameId.toString())
            val categoryNames = mutableListOf<String?>()
            val categoryIds = mutableListOf<Int?>()
            val answers = mutableListOf<String>()
            val questions = mutableListOf<String>()
            val difficulties = mutableListOf<String>()
            val correctAnswers = mutableListOf<String>()
            val incorrectAnswers = mutableListOf<List<String>>()
            val questionIds = mutableListOf<String>()
            value.questions.forEach {
                categoryNames.add(it.category?.name)
                categoryIds.add(it.category?.id)
                answers.add(it.answer.answer)
                questions.add(it.question.question)
                difficulties.add(it.question.difficulty.toString())
                correctAnswers.add(it.question.correctAnswer)
                incorrectAnswers.add(it.question.incorrectAnswers)
                questionIds.add(it.question.id.toString())
            }
            encodeSerializableElement(descriptor, 3, ListSerializer(String.serializer().nullable), categoryNames)
            encodeSerializableElement(descriptor, 4, ListSerializer(Int.serializer().nullable), categoryIds)
            encodeSerializableElement(descriptor, 5, ListSerializer(String.serializer()), answers)
            encodeSerializableElement(descriptor, 6, ListSerializer(String.serializer()), questions)
            encodeSerializableElement(descriptor, 7, ListSerializer(String.serializer()), difficulties)
            encodeSerializableElement(descriptor, 8, ListSerializer(String.serializer()), correctAnswers)
            encodeSerializableElement(descriptor, 9, ListSerializer(ListSerializer(String.serializer())), incorrectAnswers)
            encodeSerializableElement(descriptor, 10, ListSerializer(String.serializer()), questionIds)
        }
    }
}

object WrappedGameSerializer : KSerializer<WrappedGame> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WrappedGame") {
        element<String>("game")
    }

    override fun deserialize(decoder: Decoder): WrappedGame {
        return decoder.decodeStructure(descriptor) {
            var game: Game? = null

            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop
                    0 -> game = decodeSerializableElement(descriptor, 0, GameSerializer)
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            WrappedGame(requireNotNull(game))
        }
    }

    override fun serialize(encoder: Encoder, value: WrappedGame) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, GameSerializer, value.game)
        }
    }
}