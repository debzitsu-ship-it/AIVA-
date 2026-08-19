package com.aiva.core.util

import com.aiva.core.util.JsonUtil
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testJsonUtil() {
        val testObj = TestData(name = "test", value = 42)
        val jsonStr = JsonUtil.toJson(testObj)
        val decoded = JsonUtil.fromJson(jsonStr, TestData.serializer())
        assertEquals(testObj, decoded)
    }
    
    @Test
    fun testCopyWith() {
        val original = TestData(name = "original", value = 1)
        val modified = original.copyWith { name = "modified" }
        assertEquals("modified", modified.name)
        assertEquals(1, modified.value)
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