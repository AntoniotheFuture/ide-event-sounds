package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class NotificationReporter(private val project: Project) {
    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true
        println("[IDE Event Sounds] Notification reporter initialized")
    }

    fun stopListening() {
        isListening = false
        println("[IDE Event Sounds] Notification listener stopped")
    }

    fun sendTestNotification() {
        val service = project.service<EventSoundsPluginService>()
        service.triggerNotification("事件声音测试通知", "这是一条测试消息")
    }
}