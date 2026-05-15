package com.antoniofuture.ideeventsounds.core.config

data class SoundMapping(
    val eventKey: String,
    val soundPath: String,
    val name: String,
    val regex: String = "",
    val isEnabled: Boolean = true
)