package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

class TestEventsListener(private val project: Project) : SMTRunnerEventsAdapter() {
    
    override fun onTestingFinished(testsRoot: SMTestProxy.SMTestProxyWithPrinter) {
        println("[IDE Event Sounds] Testing finished")
        
        try {
            val service = project.service<EventSoundsPluginService>()
            
            val testProxy = testsRoot.proxy
            val failedCount = testProxy.defectsCount
            
            if (failedCount > 0) {
                service.triggerEvent("test.failed", "Tests failed: $failedCount failures")
            } else {
                service.triggerEvent("test.passed", "All tests passed")
            }
        } catch (e: Exception) {
            println("[IDE Event Sounds] Failed to trigger test event: ${e.message}")
        }
    }
    
    companion object {
        val TOPIC: Topic<SMTRunnerEventsListener> = SMTRunnerEventsListener.TEST_RUNNER
    }
}