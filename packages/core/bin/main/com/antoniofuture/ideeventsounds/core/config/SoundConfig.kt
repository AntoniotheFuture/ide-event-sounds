package com.antoniofuture.ideeventsounds.core.config

data class SoundConfig(
    var version: String = "0.0.4",
    var enable: Boolean = true,
    var sounds: List<SoundMapping> = emptyList()
)
