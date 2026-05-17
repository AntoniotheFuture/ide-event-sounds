package com.antoniofuture.ideeventsounds.intellij.actions

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class TestNotificationAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val service = service<EventSoundsPluginService>()
        service.triggerNotification("测试通知标题", "这是一条测试消息内容")
    }
}
