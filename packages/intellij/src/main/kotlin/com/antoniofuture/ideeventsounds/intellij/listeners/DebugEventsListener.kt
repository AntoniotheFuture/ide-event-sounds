package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class DebugEventsListener(private val project: Project) : ExecutionListener {
    
    override fun processStartScheduled(executorId: String, env: ExecutionEnvironment) {
        if (executorId == "Debug") {
            println("[IDE Event Sounds] Debug session starting")
            try {
                val service = project.service<EventSoundsPluginService>()
                service.triggerEvent("debug.started", "Debug session started")
            } catch (e: Exception) {
                println("[IDE Event Sounds] Failed to trigger debug.started: ${e.message}")
            }
        }
    }
    
    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        if (executorId == "Debug") {
            println("[IDE Event Sounds] Debug process started")
        }
    }
    
    override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
        if (executorId == "Debug") {
            println("[IDE Event Sounds] Debug session terminated with exit code: $exitCode")
            try {
                val service = project.service<EventSoundsPluginService>()
                service.triggerEvent("debug.stopped", "Debug session stopped")
            } catch (e: Exception) {
                println("[IDE Event Sounds] Failed to trigger debug.stopped: ${e.message}")
            }
        }
    }
}