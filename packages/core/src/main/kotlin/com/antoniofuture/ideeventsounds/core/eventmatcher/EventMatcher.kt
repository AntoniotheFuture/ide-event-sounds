package com.antoniofuture.ideeventsounds.core.eventmatcher

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.config.SoundMapping
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer
import java.util.concurrent.ConcurrentHashMap

class EventMatcher {
    private val configManager = ConfigManager.instance
    private val soundPlayer = SoundPlayer()
    private val regexCache = ConcurrentHashMap<String, Regex>()

    fun matchAndPlay(eventKey: String, message: String? = null) {
        val config = configManager.loadConfig()
        if (!config.enable) {
            return
        }

        if (eventKey == "notification" && message != null) {
            matchAndPlayNotification(message)
            return
        }

        val soundMapping = configManager.getSoundMapping(eventKey) ?: return

        // 检查单个事件是否启用
        if (!soundMapping.isEnabled) {
            return
        }

        // 正则匹配逻辑
        if (soundMapping.regex.isNotEmpty() && message != null) {
            if (!matchesRegex(soundMapping.regex, message)) {
                return
            }
        }

        soundPlayer.playSound(eventKey)
    }

    private fun matchAndPlayNotification(message: String) {
        val config = configManager.loadConfig()
        val notificationMappings = config.sounds.filter { it.eventKey == "notification" && it.isEnabled }

        for (soundMapping in notificationMappings) {
            if (soundMapping.regex.isNotEmpty() && matchesRegex(soundMapping.regex, message)) {
                playSoundFromMapping(soundMapping)
                return
            }
        }
    }

    private fun playSoundFromMapping(soundMapping: SoundMapping) {
        val soundPath = soundMapping.soundPath
        try {
            if (soundPath.startsWith("preset/") || soundPath.startsWith("sounds/")) {
                val prefix = if (soundPath.startsWith("preset/")) "preset/" else "sounds/"
                val fileName = soundPath.substring(prefix.length)
                val resourcePath = "/preset/$fileName"
                val inputStream = javaClass.getResourceAsStream(resourcePath)
                if (inputStream != null) {
                    soundPlayer.playFromStream(inputStream)
                }
            } else {
                val soundFile = java.io.File(soundPath)
                if (soundFile.exists()) {
                    soundPlayer.playFromFile(soundFile)
                }
            }
        } catch (e: Exception) {
            println("[EventMatcher] Error playing sound: ${e.message}")
        }
    }

    private fun matchesRegex(pattern: String, message: String): Boolean {
        return try {
            val regex = regexCache.computeIfAbsent(pattern) { Regex(it) }
            regex.containsMatchIn(message)
        } catch (e: Exception) {
            // 正则表达式无效，跳过匹配（视为匹配成功）
            println("[EventMatcher] Invalid regex pattern: $pattern, error: ${e.message}")
            true
        }
    }

    fun testRegex(pattern: String, testMessage: String): Boolean {
        return try {
            val regex = Regex(pattern)
            regex.containsMatchIn(testMessage)
        } catch (e: Exception) {
            println("[EventMatcher] Regex test failed: ${e.message}")
            false
        }
    }
}