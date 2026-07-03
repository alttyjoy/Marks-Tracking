package com.example.data.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Data Service Layer matching Crypto-JS AES encryption standard.
 * Provides mutual cryptographic compatibility with the frontend WebView's CryptoJS service.
 */
class CryptoJsDataService(private val context: Context) {
    private val algorithm = "AES/CBC/PKCS5Padding"
    // Standard keys matching CryptoJS UTF-8 parsers
    private val secretKeyString = "SaaS_Marks_Tracking_Secret_Key_1" // 32 bytes for AES-256
    private val ivString = "IV_MarksTracking" // 16 bytes IV

    /**
     * Encrypts plain text (e.g. JSON strings of student marks) before saving.
     */
    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val keyBytes = secretKeyString.toByteArray(StandardCharsets.UTF_8)
            val ivBytes = ivString.toByteArray(StandardCharsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText ?: ""
        }
    }

    /**
     * Decrypts encrypted strings (compatible with CryptoJS.AES.decrypt).
     */
    fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        return try {
            val keyBytes = secretKeyString.toByteArray(StandardCharsets.UTF_8)
            val ivBytes = ivString.toByteArray(StandardCharsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText ?: ""
        }
    }
}
