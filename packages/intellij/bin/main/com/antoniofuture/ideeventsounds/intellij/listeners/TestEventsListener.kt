package com.antoniofuture.ideeventsounds.intellij.listeners

import com.antoniofuture.ideeventsounds.intellij.plugin.EventSoundsPluginService
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.openapi.components.service
import com.intellij.util.messages.Topic

interface TestStatusListener {
    fun testSuiteFinished(testCount: Int, ignoredCount: Int, failedCount: Int, durationMillis: Long)
}

class TestEventsListener {
    companion object {
        val TOPIC = Topic.create("Test Status", TestStatusListener::class.java)
    }
}
