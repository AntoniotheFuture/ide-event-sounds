package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.compatibility.VersionCompatibility
import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.Executors

class ApplicationLifecycleListener(private val project: Project) {
    private var connection: Any? = null
    private val executor = Executors.newSingleThreadExecutor()

    init {
        subscribe()
    }

    private fun subscribe() {
        try {
            val messageBus = project.javaClass.getMethod("getMessageBus").invoke(project)
            connection = messageBus.javaClass.getMethod("connect").invoke(messageBus)

            if (VersionCompatibility.supportsApplicationLifecycleListener()) {
                val lifecycleListenerClass = Class.forName("com.intellij.openapi.application.ApplicationLifecycleListener")
                val topicField = lifecycleListenerClass.getDeclaredField("TOPIC")
                val topic = topicField.get(null)

                val listener = object : Any() {
                    @Suppress("unused")
                    fun appFrameCreated(commandLineArgs: List<String>) {
                        println("[IDE Event Sounds] Application frame created - scheduling event trigger")
                        triggerEventDelayed("application.frame.created", "Application frame created")
                    }

                    @Suppress("unused")
                    fun appStarted() {
                        println("[IDE Event Sounds] Application started - scheduling event trigger")
                        triggerEventDelayed("application.started", "Application started")
                    }

                    @Suppress("unused")
                    fun appClosing() {
                        println("[IDE Event Sounds] Application closing - scheduling event trigger")
                        triggerEventDelayed("application.closing", "Application closing")
                    }
                }

                connection?.javaClass?.getMethod("subscribe", Class.forName("com.intellij.util.messages.Topic"), Any::class.java)
                    ?.invoke(connection, topic, listener)
            }
        } catch (e: Exception) {
            println("[IDE Event Sounds] ApplicationLifecycleListener failed: ${e.message}")
        }
    }

    private fun triggerEventDelayed(eventKey: String, message: String) {
        executor.execute {
            try {
                Thread.sleep(1000)
                project.service<EventSoundsPluginService>().triggerEvent(eventKey, message)
            } catch (e: Exception) {
                println("[IDE Event Sounds] Failed to trigger $eventKey: ${e.message}")
            }
        }
    }

    fun dispose() {
        executor.shutdown()
        try {
            connection?.javaClass?.getMethod("dispose")?.invoke(connection)
        } catch (e: Exception) {
        }
    }
}