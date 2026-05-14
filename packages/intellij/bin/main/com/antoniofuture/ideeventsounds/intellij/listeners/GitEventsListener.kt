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
            val messageBus = project.javaClass.getMethod("getMessageBus").invoke(project)
            connection = messageBus.javaClass.getMethod("connect").invoke(messageBus)

            val gitCommandListenerClass = Class.forName("git4idea.commands.GitCommandListener")
            val topicField = gitCommandListenerClass.getDeclaredField("TOPIC")
            val topic = topicField.get(null)

            val listener = object : Any() {
                @Suppress("unused")
                fun commandStarted(command: Any) {
                    handleCommandStarted(command)
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

            connection?.javaClass?.getMethod("subscribe", Class.forName("com.intellij.util.messages.Topic"), Any::class.java)
                ?.invoke(connection, topic, listener)

        } catch (e: Exception) {
            println("[IDE Event Sounds] GitEventsListener subscription failed: ${e.message}")
        }
    }

    private fun handleCommandStarted(command: Any) {
        val commandName = command.toString()
        println("[IDE Event Sounds] Git command started: $commandName")

        try {
            val service = project.service<EventSoundsPluginService>()
            service.triggerEvent("git.$commandName.started", "$commandName started")
        } catch (e: Exception) {
            println("[IDE Event Sounds] Failed to trigger started event: ${e.message}")
        }
    }

    private fun handleCommandFinished(command: Any, success: Boolean) {
        val commandName = command.toString()
        val status = if (success) "success" else "failed"
        println("[IDE Event Sounds] Git command $commandName: $status")

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