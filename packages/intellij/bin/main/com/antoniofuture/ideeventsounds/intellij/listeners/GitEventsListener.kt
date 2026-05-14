package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.compatibility.VersionCompatibility
import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class GitEventsListener(private val project: Project) {
    private var connection: Any? = null

    init {
        if (VersionCompatibility.supportsGitCommandListener()) {
            subscribe()
        }
    }

    private fun subscribe() {
        try {
            // 获取 MessageBusConnection
            val messageBus = project.javaClass.getMethod("getMessageBus").invoke(project)
            connection = messageBus.javaClass.getMethod("connect").invoke(messageBus)

            // 获取 GitCommandListener.TOPIC
            val gitCommandListenerClass = Class.forName("git4idea.commands.GitCommandListener")
            val topicField = gitCommandListenerClass.getDeclaredField("TOPIC")
            val topic = topicField.get(null)

            // 创建监听器代理 - 简化版本，避免在回调中调用 service()
            val listener = object : Any() {
                @Suppress("unused")
                fun commandStarted(command: Any) {
                    val commandName = command.toString()
                    println("[IDE Event Sounds] Git command started: $commandName")
                }

                @Suppress("unused")
                fun commandFinished(command: Any, success: Boolean) {
                    handleCommandFinished(command, success)
                }

                @Suppress("unused")
                fun commandFinished(command: Any, success: Boolean, repository: Any) {
                    handleCommandFinished(command, success)
                }
            }

            // 使用反射调用 subscribe
            connection?.javaClass?.getMethod("subscribe", Class.forName("com.intellij.util.messages.Topic"), Any::class.java)
                ?.invoke(connection, topic, listener)

        } catch (e: Exception) {
            println("[IDE Event Sounds] GitEventsListener subscription failed: ${e.message}")
        }
    }

    private fun handleCommandFinished(command: Any, success: Boolean) {
        val commandName = command.toString()
        val status = if (success) "success" else "failed"
        println("[IDE Event Sounds] Git command $commandName: $status")

        // 延迟获取 service，避免在回调中死锁
        try {
            val service = project.service<EventSoundsPluginService>()
            if (success) {
                if (commandName == "CHECKOUT") {
                    service.triggerEvent("git.branch.checkedout", "Checked out branch")
                }
                service.triggerEvent("git.$commandName.success", "$commandName successful")
            } else {
                service.triggerEvent("git.$commandName.failed", "$commandName failed")
            }
        } catch (e: Exception) {
            println("[IDE Event Sounds] Failed to trigger event: ${e.message}")
        }
    }

    fun dispose() {
        try {
            connection?.javaClass?.getMethod("dispose")?.invoke(connection)
        } catch (e: Exception) {
        }
    }
}