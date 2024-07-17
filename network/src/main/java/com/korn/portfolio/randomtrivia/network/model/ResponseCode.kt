package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ResponseCodeSerializer::class)
enum class ResponseCode(val value: Int) {
    SUCCESS(0),
    NO_RESULTS(1),
    INVALID_PARAMS(2),
    TOKEN_NOT_EXIST(3),
    TOKEN_EMPTY(4),
    RATE_LIMIT(5),
    UNSUPPORTED(-1);
}

private class ResponseCodeSerializer: EnumAsIntSerializer<ResponseCode>(
    serialName = "ResponseCode",
    serialize = { it.value },
    deserialize = { v -> ResponseCode.entries.first { it.value == v } }
)

// Ref. : https://stackoverflow.com/a/75644603
private open class EnumAsIntSerializer<T: Enum<*>>(
    serialName: String,
    val serialize: (v: T) -> Int,
    val deserialize: (v: Int) -> T
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeInt(serialize(value))
    }

    override fun deserialize(decoder: Decoder): T {
        val v = decoder.decodeInt()
        return deserialize(v)
    }
}