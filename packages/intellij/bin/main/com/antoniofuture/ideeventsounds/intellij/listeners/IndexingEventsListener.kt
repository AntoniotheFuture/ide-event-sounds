package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class IndexingEventsListener(private val project: Project) {
    private var connection: Any? = null

    init {
        subscribe()
    }

    private fun subscribe() {
        try {
            // 获取 MessageBusConnection
            val messageBus = project.javaClass.getMethod("getMessageBus").invoke(project)
            connection = messageBus.javaClass.getMethod("connect").invoke(messageBus)

            // 订阅 DUMB_MODE 事件
            val dumbServiceClass = Class.forName("com.intellij.openapi.project.DumbService")
            val topicField = dumbServiceClass.getDeclaredField("DUMB_MODE")
            val topic = topicField.get(null)

            val listener = object : Any() {
                @Suppress("unused")
                fun enteredDumbMode() {
                    println("[IDE Event Sounds] Indexing started")
                    triggerEventSafe("indexing.started", "Indexing started")
                }

                @Suppress("unused")
                fun exitDumbMode() {
                    println("[IDE Event Sounds] Indexing finished")
                    triggerEventSafe("indexing.finished", "Indexing finished")
                }
            }

            connection?.javaClass?.getMethod("subscribe", Class.forName("com.intellij.util.messages.Topic"), Any::class.java)
                ?.invoke(connection, topic, listener)
        } catch (e: Exception) {
            println("[IDE Event Sounds] IndexingEventsListener failed: ${e.message}")
        }
    }

    private fun triggerEventSafe(eventKey: String, message: String) {
        try {
            project.service<EventSoundsPluginService>().triggerEvent(eventKey, message)
        } catch (e: Exception) {
            println("[IDE Event Sounds] Failed to trigger $eventKey: ${e.message}")
        }
    }

    fun dispose() {
        try {
            connection?.javaClass?.getMethod("dispose")?.invoke(connection)
        } catch (e: Exception) {
        }
    }
}