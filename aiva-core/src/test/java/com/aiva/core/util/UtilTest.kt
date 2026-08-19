package com.aiva.core.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testJsonUtil() {
        val testObj = TestData(name = "test", value = 42)
        val jsonStr = JsonUtil.toJson(testObj, TestData.serializer())
        val decoded = JsonUtil.fromJson(jsonStr, TestData.serializer())
        assertEquals(testObj, decoded)
    }

    @Test
    fun testJsonSerializationRoundTrip() {
        val data = TestData(name = "roundtrip", value = 100)
        val jsonStr = json.encodeToString(data)
        val decoded = json.decodeFromString(TestData.serializer(), jsonStr)
        assertEquals(data, decoded)
    }

    @Test
    fun testJsonWithNullValues() {
        val data = TestData(name = null, value = 0)
        val jsonStr = json.encodeToString(data)
        val decoded = json.decodeFromString(TestData.serializer(), jsonStr)
        assertEquals(data, decoded)
    }

    @kotlinx.serialization.Serializable
    data class TestData(val name: String?, val value: Int)
}
