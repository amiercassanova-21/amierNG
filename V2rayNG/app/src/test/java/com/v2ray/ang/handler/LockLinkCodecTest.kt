package com.v2ray.ang.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LockLinkCodecTest {

    private val key = ByteArray(32) { it.toByte() }
    private val otherKey = ByteArray(32) { (it + 1).toByte() }
    private val uri = "vless://7b2c1f60-0000-4000-8000-000000000001@example.com:443?security=tls&type=ws#admin"

    @Test
    fun lockedLinkRoundTripsAndHidesTheUri() {
        val link = LockLinkCodec.encode(uri, key)

        assertTrue(link.startsWith(LockLinkCodec.SCHEME))
        assertFalse(link.contains("example.com"))
        assertEquals(uri, LockLinkCodec.decode(link, key))
    }

    @Test
    fun eachLinkUsesAFreshNonce() {
        val first = LockLinkCodec.encode(uri, key)
        val second = LockLinkCodec.encode(uri, key)

        assertFalse(first == second)
        assertEquals(uri, LockLinkCodec.decode(second, key))
    }

    @Test
    fun wrongKeyOrTamperedLinkIsRejected() {
        val link = LockLinkCodec.encode(uri, key)
        val payload = link.removePrefix(LockLinkCodec.SCHEME)
        val flipped = payload.substring(0, 20) + (if (payload[20] == 'A') 'B' else 'A') + payload.substring(21)

        assertNull(LockLinkCodec.decode(link, otherKey))
        assertNull(LockLinkCodec.decode(LockLinkCodec.SCHEME + flipped, key))
    }

    @Test
    fun malformedInputIsRejected() {
        assertNull(LockLinkCodec.decode(uri, key))
        assertNull(LockLinkCodec.decode(LockLinkCodec.SCHEME, key))
        assertNull(LockLinkCodec.decode(LockLinkCodec.SCHEME + "not base64!", key))
    }
}
