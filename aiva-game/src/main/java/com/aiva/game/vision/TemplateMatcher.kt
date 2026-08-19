package com.aiva.game.vision

import android.graphics.Bitmap
import android.graphics.Rect
import java.util.concurrent.ConcurrentHashMap

class TemplateMatcher {
    
    private val templates = ConcurrentHashMap<String, Bitmap>()
    private val matchThreshold = 0.8
    
    fun addTemplate(name: String, bitmap: Bitmap) {
        templates[name] = bitmap
    }
    
    fun removeTemplate(name: String) {
        templates.remove(name)
    }
    
    fun matchAll(
        source: Bitmap,
        region: Rect? = null
    ): List<TemplateMatch> {
        val matches = mutableListOf<TemplateMatch>()
        
        templates.forEach { (name, template) ->
            val match = matchTemplate(source, template, region)
            if (match != null && match.confidence >= matchThreshold) {
                matches.add(match.copy(name = name))
            }
        }
        
        return matches
    }
    
    private fun matchTemplate(
        source: Bitmap,
        template: Bitmap,
        region: Rect?
    ): TemplateMatch? {
        // In real implementation, use OpenCV matchTemplate
        // For now, return null placeholder
        return null
    }
    
    fun findBestMatch(
        source: Bitmap,
        templateNames: List<String>,
        region: Rect? = null
    ): TemplateMatch? {
        var bestMatch: TemplateMatch? = null
        var bestConfidence = 0f
        
        templateNames.forEach { name ->
            templates[name]?.let { template ->
                val match = matchTemplate(source, template, region)
                if (match != null && match.confidence > bestConfidence) {
                    bestConfidence = match.confidence
                    bestMatch = match.copy(name = name)
                }
            }
        }
        
        return bestMatch
    }
    
    data class TemplateMatch(
        val name: String,
        val confidence: Float,
        val bounds: Rect,
        val centerX: Int,
        val centerY: Int
    ) {
        fun copy(
            name: String = this.name,
            confidence: Float = this.confidence,
            bounds: Rect = this.bounds,
            centerX: Int = this.centerX,
            centerY: Int = this.centerY
        ): TemplateMatch {
            return TemplateMatch(name, confidence, bounds, centerX, centerY)
        }
    }
}