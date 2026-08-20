package com.aiva.core.util

import android.content.Context
import android.util.Base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecureStorage {
    private val memory = ConcurrentHashMap<String, String>()

    @Volatile
    private var prefs: android.content.SharedPreferences? = null

    @Synchronized
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("aiva_prefs", Context.MODE_PRIVATE)
        }
    }

    fun putString(key: String, value: String) {
        memory[key] = value
        prefs?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return memory[key] ?: prefs?.getString(key, defaultValue) ?: defaultValue
    }

    fun putBoolean(key: String, value: Boolean) = putString(key, value.toString())

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
    }

    fun putLong(key: String, value: Long) = putString(key, value.toString())

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }

    fun remove(key: String) {
        memory.remove(key)
        prefs?.edit()?.remove(key)?.apply()
    }

    fun clear() {
        memory.clear()
        prefs?.edit()?.clear()?.apply()
    }

    fun contains(key: String): Boolean {
        return memory.containsKey(key) || (prefs?.contains(key) == true)
    }
}

object KeyStoreManager {
    @Volatile
    private var secretKey: SecretKey? = null

    @Synchronized
    fun generateKey(): String {
        if (secretKey == null) {
            val generator = KeyGenerator.getInstance("AES")
            generator.init(256, SecureRandom())
            secretKey = generator.generateKey()
        }
        return "aiva_debug_key"
    }

    fun encrypt(data: ByteArray): ByteArray {
        generateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        return iv + cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        generateKey()
        val iv = data.copyOfRange(0, 12)
        val encrypted = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }

    fun encryptString(text: String): String {
        return Base64.encodeToString(encrypt(text.toByteArray()), Base64.NO_WRAP)
    }

    fun decryptString(encrypted: String): String {
        return String(decrypt(Base64.decode(encrypted, Base64.NO_WRAP)))
    }
}

object JsonUtil {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun <T> toJson(obj: T, serializer: KSerializer<T>): String = json.encodeToString(serializer, obj)
    fun <T> fromJson(jsonStr: String, serializer: KSerializer<T>): T =
        json.decodeFromString(serializer, jsonStr)
}

inline fun <reified T> Context.getSystemServiceCompat(): T {
    return getSystemService(T::class.java)
}
