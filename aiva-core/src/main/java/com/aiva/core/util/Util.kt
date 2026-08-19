package com.aiva.core.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore

object SecureStorage {
    private const val PREFS_NAME = "aiva_secure_prefs"
    private const val MASTER_KEY_ALIAS = "aiva_master_key"
    
    private var encryptedPrefs: androidx.security.crypto.EncryptedSharedPreferences? = null
    
    fun init(context: Context) {
        if (encryptedPrefs == null) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            encryptedPrefs = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
    
    fun putString(key: String, value: String) {
        encryptedPrefs?.edit()?.putString(key, value)?.apply()
    }
    
    fun getString(key: String, defaultValue: String = ""): String {
        return encryptedPrefs?.getString(key, defaultValue) ?: defaultValue
    }
    
    fun putBoolean(key: String, value: Boolean) {
        encryptedPrefs?.edit()?.putBoolean(key, value)?.apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs?.getBoolean(key, defaultValue) ?: defaultValue
    }
    
    fun putLong(key: String, value: Long) {
        encryptedPrefs?.edit()?.putLong(key, value)?.apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedPrefs?.getLong(key, defaultValue) ?: defaultValue
    }
    
    fun remove(key: String) {
        encryptedPrefs?.edit()?.remove(key)?.apply()
    }
    
    fun clear() {
        encryptedPrefs?.edit()?.clear()?.apply()
    }
    
    fun contains(key: String): Boolean {
        return encryptedPrefs?.contains(key) ?: false
    }
}

object KeyStoreManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "aiva_encryption_key"
    
    fun generateKey(): String {
        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            "AES", KEYSTORE_PROVIDER
        )
        val keyGenParameterSpec = android.security.keystore.KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyStore.PROPERTY_ENCRYPT or KeyStore.PROPERTY_DECRYPT
        ).apply {
            setBlockModes("GCM")
            setEncryptionPaddings("NoPadding")
            setKeySize(256)
            setUserAuthenticationRequired(false)
        }.build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
        return KEY_ALIAS
    }
    
    fun encrypt(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as javax.crypto.SecretKey
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    
    fun decrypt(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as javax.crypto.SecretKey
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    
    fun encryptString(text: String): String {
        return Base64.encodeToString(encrypt(text.toByteArray()), Base64.NO_WRAP)
    }
    
    fun decryptString(encrypted: String): String {
        return String(decrypt(Base64.decode(encrypted, Base64.NO_WRAP)))
    }
}

object JsonUtil {
    val json = Json { ignoreUnknownKeys = true }
    
    fun <T> toJson(obj: T): String = json.encodeToString(obj)
    fun <T> fromJson(jsonStr: String, serialDescriptor: kotlinx.serialization.KSerializer<T>): T = 
        json.decodeFromString(serialDescriptor, jsonStr)
}

fun <T> T.copyWith(block: T.() -> Unit): T {
    val copy = this
    copy.block()
    return copy
}

inline fun <reified T> Context.getSystemService(): T {
    return getSystemService(T::class.java)
}