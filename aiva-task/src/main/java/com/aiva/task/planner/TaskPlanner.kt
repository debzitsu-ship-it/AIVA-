package com.aiva.task.planner

class TaskPlanner {
    data class PlannerContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val activeGameProfile: String? = null
    )
}
