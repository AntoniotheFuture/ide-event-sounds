package com.antoniofuture.ideeventsounds.intellij.plugin

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.eventmatcher.EventMatcher
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger

@Service
class EventSoundsPluginService {
    private val logger = logger<EventSoundsPluginService>()
    val configManager = ConfigManager()
    val eventMatcher = EventMatcher()

    init {
        logger.info("IDE Event Sounds plugin initialized")
    }

    fun triggerEvent(eventKey: String, message: String? = null) {
        logger.info("Triggering event: $eventKey")
        eventMatcher.matchAndPlay(eventKey, message)
    }
}