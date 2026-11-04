package com.v2ray.ang.handler

import com.v2ray.ang.BuildConfig
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/**
 * Encrypts a regular share URI (vless://, vmess://, …) into an amierNG-only locked link.
 *
 * The app must decrypt the link to connect, so this deters casual viewing and editing;
 * it cannot protect against someone who extracts the key from the APK.
 */
object LockLinkCodec {
    const val SCHEME = "amierng://lock/"

    private const val VERSION: Byte = 1
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128
    private val AAD = "amierNG-lock-v1".toByteArray(Charsets.UTF_8)

    fun encode(plainUri: String, key: ByteArray = buildKey()): String {
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(AAD)
        val sealed = cipher.doFinal(plainUri.toByteArray(Charsets.UTF_8))
        return SCHEME + Base64.UrlSafe.encode(byteArrayOf(VERSION) + iv + sealed)
    }

    /** Returns the original share URI, or null when the link is malformed or was made with another key. */
    fun decode(link: String, key: ByteArray = buildKey()): String? = runCatching {
        if (!link.startsWith(SCHEME)) return null
        val data = Base64.UrlSafe.decode(link.removePrefix(SCHEME).trim())
        if (data.size <= 1 + IV_SIZE || data[0] != VERSION) return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(key, "AES"),
            GCMParameterSpec(TAG_BITS, data.copyOfRange(1, 1 + IV_SIZE))
        )
        cipher.updateAAD(AAD)
        String(cipher.doFinal(data.copyOfRange(1 + IV_SIZE, data.size)), Charsets.UTF_8)
    }.getOrNull()

    private fun buildKey(): ByteArray = Base64.Default.decode(BuildConfig.LOCK_KEY)
}
