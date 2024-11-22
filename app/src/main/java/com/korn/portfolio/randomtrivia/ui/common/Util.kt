package com.korn.portfolio.randomtrivia.ui.common

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json

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
        element<String?>("category", isOptional = true)
        element<String?>("difficulty", isOptional = true)
        element<String>("amount")
    }

    override fun deserialize(decoder: Decoder): GameSetting {
        return decoder.decodeStructure(descriptor) {
            var category: Category? = null
            var difficulty: Difficulty? = null
            var amount = 0

            loop@ while(true) {
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop
                    0 -> category =
                        decodeNullableSerializableElement(descriptor, 0, CategorySerializer.nullable)
                    1 -> difficulty =
                        decodeNullableSerializableElement(descriptor, 1, String.serializer().nullable)
                            ?.let { Difficulty.valueOf(it) }
                    2 -> amount = decodeIntElement(descriptor, 2)
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            GameSetting(
                category = category,
                difficulty  = difficulty,
                amount = amount
            )
        }
    }

    override fun serialize(encoder: Encoder, value: GameSetting) {
        encoder.encodeStructure(descriptor) {
            encodeNullableSerializableElement(descriptor, 0, CategorySerializer.nullable, value.category)
            encodeNullableSerializableElement(descriptor, 1, String.serializer().nullable, value.difficulty?.toString())
            encodeIntElement(descriptor, 2, value.amount)
        }
    }
}

object CategorySerializer : KSerializer<Category> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Category {
        return Json.decodeFromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Category) {
        encoder.encodeString(Json.encodeToString(value))
    }
}