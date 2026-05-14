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

        // 注册Git事件监听器
        try {
            logger.error("DEBUG: Creating GitEventsListener for project: ${project.name}")
            val gitListener = GitEventsListener(project)
            com.intellij.openapi.util.Disposer.register(project, com.intellij.openapi.util.Disposable {
                gitListener.dispose()
            })
            logger.error("DEBUG: GitEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create GitEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册应用生命周期监听器
        try {
            logger.error("DEBUG: Creating ApplicationLifecycleListener for project: ${project.name}")
            val appListener = ApplicationLifecycleListener(project)
            com.intellij.openapi.util.Disposer.register(project, com.intellij.openapi.util.Disposable {
                appListener.dispose()
            })
            logger.error("DEBUG: ApplicationLifecycleListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create ApplicationLifecycleListener for project ${project.name}: ${e.message}", e)
        }

        // 注册索引事件监听器
        try {
            logger.error("DEBUG: Creating IndexingEventsListener for project: ${project.name}")
            val indexingListener = IndexingEventsListener(project)
            com.intellij.openapi.util.Disposer.register(project, com.intellij.openapi.util.Disposable {
                indexingListener.dispose()
            })
            logger.error("DEBUG: IndexingEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create IndexingEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册测试事件监听器
        try {
            logger.error("DEBUG: Creating TestEventsListener for project: ${project.name}")
            val testListener = TestEventsListener(project)
            val messageBus = project.messageBus.connect()
            messageBus.subscribe(TestEventsListener.TOPIC, testListener)
            com.intellij.openapi.util.Disposer.register(project, com.intellij.openapi.util.Disposable {
                messageBus.dispose()
            })
            logger.error("DEBUG: TestEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create TestEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册调试事件监听器
        try {
            logger.error("DEBUG: Creating DebugEventsListener for project: ${project.name}")
            val debugListener = DebugEventsListener(project)
            val messageBus = project.messageBus.connect()
            messageBus.subscribe(com.intellij.execution.ExecutionListener.TOPIC, debugListener)
            com.intellij.openapi.util.Disposer.register(project, com.intellij.openapi.util.Disposable {
                messageBus.dispose()
            })
            logger.error("DEBUG: DebugEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create DebugEventsListener for project ${project.name}: ${e.message}", e)
        }
    }
}