package com.app.triflow.data.local.db.converters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class RoomConverters {

    private val stringListSerializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        Json.encodeToString(stringListSerializer, value.orEmpty())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            Json.decodeFromString(stringListSerializer, value)
        }.getOrDefault(emptyList())
    }
}
