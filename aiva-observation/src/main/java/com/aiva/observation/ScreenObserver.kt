package com.aiva.observation

import com.aiva.core.observation.ObservationResult
import com.aiva.core.observation.ScreenState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenObserver @Inject constructor() {
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
