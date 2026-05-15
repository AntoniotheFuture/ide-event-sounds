package com.antoniofuture.ideeventsounds.core.config

data class SoundConfig(
    val version: String = "0.0.3",
    val enable: Boolean = true,
    val sounds: List<SoundMapping> = emptyList()
)
