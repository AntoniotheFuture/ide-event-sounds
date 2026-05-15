package com.antoniofuture.ideeventsounds.core.eventmatcher

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer
import java.util.concurrent.ConcurrentHashMap

class EventMatcher {
    private val configManager = ConfigManager()
    private val soundPlayer = SoundPlayer()
    private val regexCache = ConcurrentHashMap<String, Regex>()

    fun matchAndPlay(eventKey: String, message: String? = null) {
        val soundMapping = configManager.getSoundMapping(eventKey) ?: return

        // 检查全局开关
        val config = configManager.loadConfig()
        if (!config.enable) {
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