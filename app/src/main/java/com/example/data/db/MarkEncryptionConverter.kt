package com.example.data.db

import androidx.room.TypeConverter
import com.example.util.EncryptionUtil

class MarkEncryptionConverter {
    @TypeConverter
    fun fromDouble(value: Double?): String? {
        if (value == null) return null
        return EncryptionUtil.encrypt(value.toString())
    }

    @TypeConverter
    fun toDouble(value: String?): Double? {
        if (value == null) return null
        val decrypted = EncryptionUtil.decrypt(value)
        return decrypted.toDoubleOrNull() ?: 0.0
    }
}
