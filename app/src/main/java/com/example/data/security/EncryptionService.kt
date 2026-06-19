package com.example.data.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Service interface for securing sensitive student data before saving it to
 * the database, and retrieving/decrypting it for user presentation.
 */
interface EncryptionService {
    fun encrypt(plainText: String?): String
    fun decrypt(encryptedText: String?): String
    fun isCorrupted(text: String): Boolean
    fun setOnDecryptionError(listener: (String, Throwable) -> Unit)
}

/**
 * Standard AES/CBC/PKCS5Padding encryption service implementation.
 */
class AesEncryptionService : EncryptionService {
    private val algorithm = "AES/CBC/PKCS5Padding"
    private val keyBytes = "MarksTrack123456".toByteArray(StandardCharsets.UTF_8)
    private val ivBytes = "IV_MarksTracking".toByteArray(StandardCharsets.UTF_8)
    private var errorListener: ((String, Throwable) -> Unit)? = null

    override fun setOnDecryptionError(listener: (String, Throwable) -> Unit) {
        errorListener = listener
    }

    override fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText ?: ""
        }
    }

    override fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        return try {
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8).trim()
        } catch (e: Exception) {
            errorListener?.invoke(encryptedText, e)
            encryptedText ?: ""
        }
    }

    override fun isCorrupted(text: String): Boolean {
        if (text.isBlank()) return false
        if (text.contains("\uFFFD")) return true
        
        var controlOrExtCount = 0
        var lettersOrDigitsOrSpaces = 0
        for (i in 0 until text.length) {
            val char = text[i]
            val code = char.code
            if (code < 32 || code == 127 || (code in 128..255 && char != 'é' && char != 'á' && char != 'ó' && char != 'í' && char != 'ú' && char != 'ñ')) {
                controlOrExtCount++
            }
            if (char.isLetterOrDigit() || char.isWhitespace()) {
                lettersOrDigitsOrSpaces++
            }
        }
        
        if (controlOrExtCount > 0) return true
        if (text.isNotEmpty() && (lettersOrDigitsOrSpaces.toFloat() / text.length) < 0.75f) {
            return true
        }
        return false
    }
}
