package com.aiva.observation

import com.aiva.core.observation.ObservationResult
import com.aiva.core.observation.ScreenState

class ScreenObserver constructor() {
    fun observe(): ObservationResult {
        return ObservationResult(
            screenState = ScreenState(
                packageName = null,
                activityName = null,
                nodes = emptyList()
            )
        )
    }
}
