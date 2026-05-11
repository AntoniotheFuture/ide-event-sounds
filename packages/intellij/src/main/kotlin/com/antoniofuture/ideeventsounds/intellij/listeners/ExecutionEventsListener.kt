package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service

class ExecutionEventsListener : ExecutionListener {
    override fun processStarting(executorId: String, env: ExecutionEnvironment) {
    }

    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        val service = service<EventSoundsPluginService>()
        service.triggerEvent("run.start", "Application started")
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        val service = service<EventSoundsPluginService>()
        service.triggerEvent("run.stop", "Application stopped with exit code $exitCode")
    }
}
