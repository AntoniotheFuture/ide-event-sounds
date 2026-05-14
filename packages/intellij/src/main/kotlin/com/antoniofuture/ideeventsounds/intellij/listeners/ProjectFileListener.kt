package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.diagnostic.logger

class ProjectFileListener(private val project: Project) : VirtualFileListener {
    private val logger = logger<ProjectFileListener>()
    
    private val ignoredPaths = listOf(
        "/.idea/",
        "/build/",
        "/out/",
        "/target/",
        ".index/",
        ".git/"
    )

    init {
        logger.error("DEBUG: ProjectFileListener initialized for project: ${project.name}")
    }

    override fun fileCreated(event: VirtualFileEvent) {
        processFileEvent(event, "file.created")
    }

    override fun fileDeleted(event: VirtualFileEvent) {
        processFileEvent(event, "file.deleted")
    }

    private fun processFileEvent(event: VirtualFileEvent, eventType: String) {
        val file = event.file ?: return
        val path = file.canonicalPath ?: return

        // 过滤掉系统文件和临时文件
        if (isSystemFile(path)) {
            return
        }

        // 只处理项目内的文件
        if (!isInProject(path)) {
            return
        }

        try {
            logger.error("DEBUG: File event detected: $eventType - ${file.name}")
            val service = project.service<EventSoundsPluginService>()
            service.triggerEvent(eventType, "File $eventType: ${file.name}")
        } catch (e: Exception) {
            logger.error("Failed to trigger $eventType: ${e.message}", e)
        }
    }

    private fun isSystemFile(path: String): Boolean {
        return ignoredPaths.any { path.contains(it) } ||
               path.endsWith(".tmp") ||
               path.endsWith(".cache") ||
               path.contains("/system/")
    }

    private fun isInProject(path: String): Boolean {
        val basePath = project.basePath ?: return false
        return path.startsWith(basePath)
    }
}