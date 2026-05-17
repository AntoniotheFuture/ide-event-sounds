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
        logger.debug("ProjectOpenListener.projectOpened called for: ${project.name}")

        val service = service<EventSoundsPluginService>()
        service.triggerEvent("project.opened", "Project opened: ${project.name}")

        // 注册项目级别的文件监听器
        try {
            logger.debug("Creating ProjectFileListener for project: ${project.name}")
            val fileListener = ProjectFileListener(project)
            VirtualFileManager.getInstance().addVirtualFileListener(fileListener, project)
            logger.debug("ProjectFileListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create ProjectFileListener for project ${project.name}: ${e.message}", e)
        }

        // 注册Git事件监听器
        try {
            logger.debug("Creating GitEventsListener for project: ${project.name}")
            val gitListener = GitEventsListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { gitListener.dispose() }
            })
            logger.debug("GitEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create GitEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册应用生命周期监听器
        try {
            logger.debug("Creating ApplicationLifecycleListener for project: ${project.name}")
            val appListener = ApplicationLifecycleListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { appListener.dispose() }
            })
            logger.debug("ApplicationLifecycleListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create ApplicationLifecycleListener for project ${project.name}: ${e.message}", e)
        }

        // 注册索引事件监听器
        try {
            logger.debug("Creating IndexingEventsListener for project: ${project.name}")
            val indexingListener = IndexingEventsListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { indexingListener.dispose() }
            })
            logger.debug("IndexingEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create IndexingEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册测试事件监听器
        try {
            logger.debug("Creating TestEventsListener for project: ${project.name}")
            val testListener = TestEventsListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { }
            })
            logger.debug("TestEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create TestEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册调试事件监听器
        try {
            logger.debug("Creating DebugEventsListener for project: ${project.name}")
            val debugListener = DebugEventsListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { }
            })
            logger.debug("DebugEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Failed to create DebugEventsListener for project ${project.name}: ${e.message}", e)
        }
    }

    private fun registerDisposable(project: Project, disposable: Any) {
        try {
            val disposerClass = Class.forName("com.intellij.openapi.util.Disposer")
            val registerMethod = disposerClass.getMethod("register", Any::class.java, Class.forName("com.intellij.openapi.util.Disposable"))
            val disposableClass = Class.forName("com.intellij.openapi.util.Disposable")
            val proxy = java.lang.reflect.Proxy.newProxyInstance(
                disposableClass.classLoader,
                arrayOf(disposableClass)
            ) { _, method, _ ->
                if (method.name == "dispose") {
                    disposable.javaClass.getMethod("dispose").invoke(disposable)
                }
                null
            }
            registerMethod.invoke(null, project, proxy)
        } catch (e: Exception) {
            logger.error("Failed to register disposable: ${e.message}", e)
        }
    }
}