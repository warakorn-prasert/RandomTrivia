package com.korn.portfolio.randomtrivia.ui.common

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
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
        element<String?>("categoryName", isOptional = true)
        element<String?>("categoryId", isOptional = true)
        element<String?>("difficulty", isOptional = true)
        element<String>("amount")
    }

    override fun deserialize(decoder: Decoder): GameSetting {
        return decoder.decodeStructure(descriptor) {
            var categoryName: String? = null
            var categoryId: Int? = null
            var difficulty: Difficulty? = null
            var amount = 0

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
            }

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