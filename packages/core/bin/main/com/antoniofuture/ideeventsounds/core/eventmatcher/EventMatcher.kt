package com.antoniofuture.ideeventsounds.core.eventmatcher

import com.antoniofuture.ideeventsounds.core.config.ConfigManager
import com.antoniofuture.ideeventsounds.core.soundplayer.SoundPlayer

class EventMatcher {
    private val configManager = ConfigManager()
    private val soundPlayer = SoundPlayer()

    fun matchAndPlay(eventKey: String, message: String? = null) {
        val soundMapping = configManager.getSoundMapping(eventKey) ?: return

        if (soundMapping.regex.isNotEmpty() && message != null) {
            val regex = Regex(soundMapping.regex)
            if (!regex.containsMatchIn(message)) {
                return
            }
        }

        soundPlayer.playSound(eventKey)
    }
}
