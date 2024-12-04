package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMintedTokenList(value: List<MintedToken>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMintedTokenList(value: String): List<MintedToken> {
        val type = object : TypeToken<List<MintedToken>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }
}