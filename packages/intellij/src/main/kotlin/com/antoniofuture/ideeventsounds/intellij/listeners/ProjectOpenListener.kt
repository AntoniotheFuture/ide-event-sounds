package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.diagnostic.logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
            registerDisposable(project, object : Any() {
                fun dispose() { gitListener.dispose() }
            })
            logger.error("DEBUG: GitEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create GitEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册应用生命周期监听器
        try {
            logger.error("DEBUG: Creating ApplicationLifecycleListener for project: ${project.name}")
            val appListener = ApplicationLifecycleListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { appListener.dispose() }
            })
            logger.error("DEBUG: ApplicationLifecycleListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create ApplicationLifecycleListener for project ${project.name}: ${e.message}", e)
        }

        // 注册索引事件监听器
        try {
            logger.error("DEBUG: Creating IndexingEventsListener for project: ${project.name}")
            val indexingListener = IndexingEventsListener(project)
            registerDisposable(project, object : Any() {
                fun dispose() { indexingListener.dispose() }
            })
            logger.error("DEBUG: IndexingEventsListener registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create IndexingEventsListener for project ${project.name}: ${e.message}", e)
        }

        // 注册通知监听器（用于正则匹配测试）
        try {
            logger.error("DEBUG: Creating NotificationReporter for project: ${project.name}")
            val notificationReporter = NotificationReporter(project)
            notificationReporter.startListening()

            // 启动5秒后发送测试通知
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.schedule({
                try {
                    logger.error("DEBUG: Sending test.notification event for regex test")
                    notificationReporter.sendTestNotification()
                    logger.error("DEBUG: test.notification event sent successfully")
                } catch (e: Exception) {
                    logger.error("DEBUG: Failed to send test.notification event: ${e.message}", e)
                } finally {
                    executor.shutdown()
                }
            }, 5, TimeUnit.SECONDS)

            registerDisposable(project, object : Any() {
                fun dispose() { notificationReporter.stopListening() }
            })
            logger.error("DEBUG: NotificationReporter registered successfully for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("DEBUG: Failed to create NotificationReporter for project ${project.name}: ${e.message}", e)
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
            logger.error("DEBUG: Failed to register disposable: ${e.message}", e)
        }
    }
}