package com.aiva.core.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import java.security.KeyStore

object SecureStorage {
    private const val PREFS_NAME = "aiva_secure_prefs"

    @Volatile
    private var encryptedPrefs: EncryptedSharedPreferences? = null

    @Synchronized
    fun init(context: Context) {
        if (encryptedPrefs == null) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            encryptedPrefs = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKey,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
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
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return KEY_ALIAS
        }
        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(false)
        }.build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
        return KEY_ALIAS
    }

    fun encrypt(data: ByteArray): ByteArray {
        generateKey()
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as javax.crypto.SecretKey
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    fun decrypt(data: ByteArray): ByteArray {
        generateKey()
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as javax.crypto.SecretKey
        val iv = data.copyOfRange(0, 12)
        val encrypted = data.copyOfRange(12, data.size)
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, spec)
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
