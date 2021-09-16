package com.example.testapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


object SecureDataFactory {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE_M_ABOVE: String = "AES/GCM/NoPadding"
    private const val AES_MODE_M_BELOW: String = "AES/ECB/PKCS7Padding"
    private const val KEY_ALIAS = "MZA-CMPak"
    private const val MZA_KEY = "mza_key"
    private const val MZA_SHARED_PREFERENCES = "preferences_mza_key"
    private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
    private const val IV = "Yp3s6v9yYp3s"
    private const val MODE_PROVIDER = "AndroidOpenSSL"
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun initKeyStore(context: Context) {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                generateKeyPairAfterM()
            } else {
                generateKeyPairBeforeM(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKeyPairAfterM() {
        val keyGenerator1: KeyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator1.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()
        )
        keyGenerator1.generateKey()
    }

    private fun generateKeyPairBeforeM(context: Context) {
        val start: Calendar = Calendar.getInstance()
        val end: Calendar = Calendar.getInstance()
        end.add(Calendar.YEAR, 100)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSubject(X500Principal("CN=$KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
        val kpg: KeyPairGenerator =
            KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE)
        kpg.initialize(spec)
        kpg.generateKeyPair()

    }

    private fun getSecretKey(): Key {
        return keyStore.getKey(KEY_ALIAS, null)
    }


    private fun encryptData(plainText: String): String {
        val c: Cipher = Cipher.getInstance(AES_MODE_M_ABOVE)
        c.init(
            Cipher.ENCRYPT_MODE,
            getSecretKey(),
            GCMParameterSpec(128, IV.toByteArray())
        )
        val encodedBytes: ByteArray = c.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
    }

    private fun decryptData(cipherText: String): String {
        val c = Cipher.getInstance(AES_MODE_M_ABOVE)
        c.init(
            Cipher.DECRYPT_MODE,
            getSecretKey(),
            GCMParameterSpec(128, IV.toByteArray())
        )
        val plainText = c.doFinal(Base64.decode(cipherText, Base64.DEFAULT))
        return String(plainText)

    }


    private fun rsaEncrypt(mSecretKey: ByteArray): ByteArray {
        val privateKeyEntry =
            keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        val inputCipher = Cipher.getInstance(RSA_MODE, MODE_PROVIDER)
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)
        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(mSecretKey)
        cipherOutputStream.close()
        return outputStream.toByteArray()
    }

    private fun rsaDecrypt(cipherBytes: ByteArray): ByteArray {
        val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        val output = Cipher.getInstance(RSA_MODE, MODE_PROVIDER)
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
        val cipherInputStream =
            CipherInputStream(ByteArrayInputStream(cipherBytes), output)
        val values: ArrayList<Byte> = ArrayList()
        var nextByte: Int
        while (cipherInputStream.read().also { nextByte = it } != -1) {
            values.add(nextByte.toByte())
        }
        val bytes = ByteArray(values.size)
        for (i in bytes.indices) {
            bytes[i] = values[i]
        }
        return bytes

    }


    private fun getOrCreateAesKey(context: Context): String {
        val pref: SharedPreferences =
            context.getSharedPreferences(MZA_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        var encryptedKeyB64 = pref.getString(MZA_KEY, null)
        if (encryptedKeyB64 == null) {
            val key = ByteArray(16)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(key)
            val encryptedKey = rsaEncrypt(key)
            encryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
            val edit = pref.edit()
            edit.putString(MZA_KEY, encryptedKeyB64)
            edit.apply()
            return encryptedKeyB64
        }
        return encryptedKeyB64
    }

    private fun getSecretKey(context: Context): Key {
        val encryptedKeyB64 = getOrCreateAesKey(context)
        val encryptedKey: ByteArray = Base64.decode(encryptedKeyB64, Base64.DEFAULT)
        val key: ByteArray = rsaDecrypt(encryptedKey)
        return SecretKeySpec(key, "AES")
    }


    @SuppressLint("DeprecatedProvider")
    private fun aesEncrypt(context: Context, input: ByteArray): String {
        val c = Cipher.getInstance(AES_MODE_M_BELOW, "BC")
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context))
        val encodedBytes = c.doFinal(input)
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
    }

    @SuppressLint("DeprecatedProvider")
    private fun aesDecrypt(context: Context, encrypted: ByteArray): String {
        val c = Cipher.getInstance(AES_MODE_M_BELOW, "BC")
        c.init(Cipher.DECRYPT_MODE, getSecretKey(context))
        val decrypted = c.doFinal(encrypted)
        return String(decrypted)
    }

    fun String.encrypt(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encryptData(this)
        } else {
            aesEncrypt(context, this.toByteArray())
        }
    }

    fun String.decrypt(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decryptData(this)
        } else {
            val a = Base64.decode(this, Base64.DEFAULT)
            aesDecrypt(context, a)
        }
    }

}

