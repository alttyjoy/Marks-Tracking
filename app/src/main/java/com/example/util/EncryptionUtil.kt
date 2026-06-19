package com.example.util

import com.example.data.security.EncryptionService
import com.example.data.security.AesEncryptionService

object EncryptionUtil {
    private val service: EncryptionService = AesEncryptionService()

    fun setOnDecryptionError(listener: (String, Throwable) -> Unit) {
        service.setOnDecryptionError(listener)
    }

    fun encrypt(plainText: String?): String {
        return service.encrypt(plainText)
    }

    fun decrypt(encryptedText: String?): String {
        return service.decrypt(encryptedText)
    }

    fun isCorrupted(text: String): Boolean {
        return service.isCorrupted(text)
    }
}

