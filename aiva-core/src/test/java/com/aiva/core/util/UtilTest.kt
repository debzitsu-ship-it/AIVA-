package com.aiva.core.util

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilTest {
    @Serializable
    data class Sample(val value: String)

    @Test
    fun jsonRoundTrip() {
        val encoded = JsonUtil.toJson(Sample("x"), Sample.serializer())
        assertEquals("x", JsonUtil.fromJson(encoded, Sample.serializer()).value)
    }
}
