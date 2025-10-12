package com.example.photonest.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<ArrayList<String>>(value, object : TypeToken<ArrayList<String>>() {}.type) ?: arrayListOf()
        } catch (e: Exception) {
            arrayListOf()
        }
    }
}
