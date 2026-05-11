package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.task.ProjectTaskContext
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class BuildEventsListener(private val project: Project) : ProjectTaskListener {
    private var buildContext: ProjectTaskContext? = null

    override fun started(context: ProjectTaskContext) {
        buildContext = context
    }

    override fun finished(result: ProjectTaskManager.Result) {
        val currentContext = result.context
        if (currentContext != buildContext) {
            return
        }
        buildContext = null

        val service = service<EventSoundsPluginService>()
        if (result.hasErrors()) {
            service.triggerEvent("build.failed", "Build failed")
        } else {
            service.triggerEvent("build.success", "Build successful")
        }
    }
}