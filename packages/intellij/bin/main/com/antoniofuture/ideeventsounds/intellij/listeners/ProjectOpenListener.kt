package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.diagnostic.logger

class ProjectOpenListener : ProjectManagerListener {
    private val logger = logger<ProjectOpenListener>()
    
    override fun projectOpened(project: Project) {
        logger.error("DEBUG: ProjectOpenListener.projectOpened called for: ${project.name}")
        
        val service = service<EventSoundsPluginService>()
        service.triggerEvent("project.opened", "Project opened: ${project.name}")

        // 注册项目级别的文件监听器
        try {
            logger.error("DEBUG: Creating ProjectFileListener for project: ${project.name}")
            val fileListener = ProjectFileListener(project)
            VirtualFileManager.getInstance().addVirtualFileListener(fileListener, project)
            logger.error("DEBUG: ProjectFileListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create ProjectFileListener for project ${project.name}: ${e.message}", e)
        }
    }
}