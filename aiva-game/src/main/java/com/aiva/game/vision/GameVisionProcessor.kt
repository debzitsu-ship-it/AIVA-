package com.aiva.game.vision

import com.aiva.core.game.FrameData
import com.aiva.core.game.GameProfile
import com.aiva.core.observation.VisionDetection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameVisionProcessor @Inject constructor() {

    fun processFrame(
        frameData: FrameData,
        profile: GameProfile
    ): List<VisionDetection> {
        if (frameData.data.isEmpty()) return emptyList()
        return emptyList()
    }
}
