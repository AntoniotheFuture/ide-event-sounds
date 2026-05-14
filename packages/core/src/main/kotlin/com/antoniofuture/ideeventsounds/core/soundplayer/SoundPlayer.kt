package com.antoniofuture.ideeventsounds.core.soundplayer

import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener

class SoundPlayer {
    private val configManager = com.antoniofuture.ideeventsounds.core.config.ConfigManager()
    private var currentClip: Clip? = null
    private val lock = Any()
    
    init {
        println("[SoundPlayer] Initialized")
    }

    private fun getResourcesDir(): String? {
        return System.getProperty("project.resources")
    }

    fun playSound(eventKey: String) {
        println("[SoundPlayer] playSound called for eventKey: $eventKey")
        
        val config = configManager.loadConfig()
        println("[SoundPlayer] Config loaded, enable=${config.enable}")
        
        if (!config.enable) {
            println("[SoundPlayer] Sound is disabled, skipping")
            return
        }

        val soundMapping = configManager.getSoundMapping(eventKey)
        println("[SoundPlayer] Sound mapping for $eventKey: $soundMapping")
        
        if (soundMapping == null) {
            println("[SoundPlayer] No sound mapping found for $eventKey")
            return
        }

        try {
            val soundPath = soundMapping.soundPath
            println("[SoundPlayer] Sound path: $soundPath")
            
            if (soundPath.startsWith("preset/")) {
                println("[SoundPlayer] Playing preset sound")
                playPresetSound(soundPath.substring("preset/".length))
            } else {
                // 尝试从资源目录加载
                val resourceDir = getResourcesDir()
                val soundFile = if (resourceDir != null) {
                    File(resourceDir, soundPath)
                } else {
                    File(soundPath)
                }
                
                println("[SoundPlayer] Resolved sound file: ${soundFile.absolutePath}, exists=${soundFile.exists()}")
                
                if (soundFile.exists()) {
                    println("[SoundPlayer] Playing sound file...")
                    playWavFile(soundFile)
                    println("[SoundPlayer] Play initiated")
                } else {
                    // 如果文件不存在，尝试从classpath资源加载
                    println("[SoundPlayer] Sound file not found, trying classpath resource...")
                    val resourcePath = "/$soundPath"
                    val inputStream = javaClass.getResourceAsStream(resourcePath)
                    if (inputStream != null) {
                        println("[SoundPlayer] Found resource: $resourcePath")
                        playFromStream(inputStream)
                        println("[SoundPlayer] Play initiated from resource")
                    } else {
                        println("[SoundPlayer] Sound file does not exist: ${soundFile.absolutePath}")
                        println("[SoundPlayer] Resource also not found: $resourcePath")
                    }
                }
            }
        } catch (e: Exception) {
            println("[SoundPlayer] Error playing sound: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun playPresetSound(fileName: String) {
        val resourcePath = "/preset/$fileName"
        println("[SoundPlayer] Loading preset sound from resource: $resourcePath")
        
        val inputStream = javaClass.getResourceAsStream(resourcePath)
        if (inputStream != null) {
            println("[SoundPlayer] Resource found, playing...")
            playFromStream(inputStream)
            println("[SoundPlayer] Play initiated from resource")
        } else {
            println("[SoundPlayer] Resource not found: $resourcePath")
        }
    }

    private fun playWavFile(file: File) {
        stopCurrentSound()

        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val clip = AudioSystem.getClip()

        synchronized(lock) {
            currentClip = clip
        }

        clip.addLineListener { event ->
            println("[SoundPlayer] Line event: ${event.type}")
            if (event.type == LineEvent.Type.STOP) {
                clip.close()
                synchronized(lock) {
                    if (currentClip == clip) {
                        currentClip = null
                    }
                }
            }
        }
        
        clip.open(audioInputStream)
        clip.start()
    }

    private fun stopCurrentSound() {
        synchronized(lock) {
            currentClip?.let { clip ->
                try {
                    if (clip.isOpen) {
                        clip.stop()
                        clip.close()
                    }
                } catch (e: Exception) {
                }
                currentClip = null
            }
        }
    }

    fun playFromStream(inputStream: InputStream) {
        stopCurrentSound()

        val bufferedInputStream = BufferedInputStream(inputStream)
        val audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
        val clip = AudioSystem.getClip()

        synchronized(lock) {
            currentClip = clip
        }

        clip.addLineListener { event ->
            println("[SoundPlayer] Line event: ${event.type}")
            if (event.type == LineEvent.Type.STOP) {
                clip.close()
                synchronized(lock) {
                    if (currentClip == clip) {
                        currentClip = null
                    }
                }
            }
        }
        
        clip.open(audioInputStream)
        clip.start()
    }
}