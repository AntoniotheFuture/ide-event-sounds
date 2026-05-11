package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.openapi.components.service
import com.intellij.util.messages.Topic

interface CompileStatusListener {
    fun compilationFinished(success: Boolean, errors: Int, warnings: Int)
}

class CompileEventsListener {
    companion object {
        val TOPIC = Topic.create("Compiler Status", CompileStatusListener::class.java)
    }
}
