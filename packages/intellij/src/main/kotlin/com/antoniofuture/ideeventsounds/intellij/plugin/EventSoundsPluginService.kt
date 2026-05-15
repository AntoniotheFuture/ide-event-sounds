package com.antoniofuture.ideeventsounds.intellij.plugin

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.config.SoundMapping
import com.antoniofuture.ideeventsounds.core.eventmatcher.EventMatcher
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger

@Service
class EventSoundsPluginService {
    private val logger = logger<EventSoundsPluginService>()
    private val configManager = ConfigManager()
    private val eventMatcher = EventMatcher()
    private val soundPlayer = SoundPlayer()

    init {
        logger.info("IDE Event Sounds plugin initialized")
    }

    fun triggerEvent(eventKey: String, message: String? = null) {
        logger.info("Triggering event: $eventKey")
        eventMatcher.matchAndPlay(eventKey, message)
    }

    fun triggerNotification(title: String, content: String) {
        logger.info("Processing notification: $title")
        
        try {
            val fullMessage = "$title $content"
            val config = configManager.loadConfig()
            if (!config.enable) return

            for (soundMapping in config.sounds) {
                if (soundMapping.regex.isEmpty()) continue
                if (!soundMapping.isEnabled) continue

                if (matchesRegex(soundMapping.regex, fullMessage)) {
                    logger.info("Notification regex matched for: ${soundMapping.eventKey}")
                    soundPlayer.playSound(soundMapping.eventKey)
                    return
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing notification: ${e.message}")
        }
    }

    private fun matchesRegex(pattern: String, message: String): Boolean {
        return try {
            val regex = Regex(pattern)
            regex.containsMatchIn(message)
        } catch (e: Exception) {
            logger.error("Invalid regex pattern: $pattern, error: ${e.message}")
            false
        }
    }
}