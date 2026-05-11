package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class ProjectOpenListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        val service = service<EventSoundsPluginService>()
        service.triggerEvent("project.opened", "Project opened: ${project.name}")
    }
}