package com.antoniofuture.ideeventsounds.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantReadWriteLock

class ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var cachedConfig: SoundConfig? = null
    private val lock = ReentrantReadWriteLock()

    private fun isDebugMode(): Boolean {
        return System.getProperty("project.config") != null
    }

    private fun getDebugConfigFile(): File? {
        val configPath = System.getProperty("project.config")
        return if (configPath != null) File(configPath) else null
    }

    fun getConfigDir(): File {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".ide-event-sounds")
    }

    fun getConfigFile(): File {
        if (isDebugMode()) {
            val debugConfig = getDebugConfigFile()
            if (debugConfig != null && debugConfig.exists()) {
                println("[ConfigManager] Using debug config: ${debugConfig.absolutePath}")
                return debugConfig
            }
        }
        return File(getConfigDir(), "config.json")
    }

    fun loadConfig(): SoundConfig {
        // 优先使用缓存
        lock.readLock().lock()
        try {
            if (cachedConfig != null) {
                return cachedConfig!!
            }
        } finally {
            lock.readLock().unlock()
        }

        // 缓存为空，需要读取
        lock.writeLock().lock()
        try {
            // 双重检查
            if (cachedConfig != null) {
                return cachedConfig!!
            }

            val configFile = getConfigFile()
            val config = if (configFile.exists()) {
                try {
                    FileReader(configFile).use { reader ->
                        val loaded = gson.fromJson(reader, SoundConfig::class.java)
                        upgradeConfig(loaded)
                    }
                } catch (e: Exception) {
                    println("[ConfigManager] Error loading config, using default: ${e.message}")
                    createDefaultConfig()
                }
            } else {
                createDefaultConfig()
            }

            cachedConfig = config
            return config
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun upgradeConfig(existingConfig: SoundConfig): SoundConfig {
        val defaultSounds = listOf(
            SoundMapping("build.success", "preset/build_success.wav", "构建成功"),
            SoundMapping("build.failed", "preset/build_failed.wav", "构建失败"),
            SoundMapping("run.start", "preset/run_start.wav", "运行启动"),
            SoundMapping("run.stop", "preset/run_stop.wav", "运行终止"),
            SoundMapping("compile.finished", "preset/compile_finished.wav", "编译完成"),
            SoundMapping("test.passed", "preset/test_passed.wav", "测试成功"),
            SoundMapping("test.failed", "preset/test_failed.wav", "测试失败"),
            SoundMapping("project.opened", "preset/project_opened.wav", "项目打开")
        )

        val existingKeys = existingConfig.sounds.map { it.eventKey }.toSet()
        val missingSounds = defaultSounds.filter { !existingKeys.contains(it.eventKey) }

        if (missingSounds.isNotEmpty()) {
            val updatedSounds = existingConfig.sounds + missingSounds
            val updatedConfig = SoundConfig(
                version = existingConfig.version,
                enable = existingConfig.enable,
                sounds = updatedSounds
            )
            saveConfigSilently(updatedConfig)
            return updatedConfig
        }

        return existingConfig
    }

    fun saveConfig(config: SoundConfig) {
        saveConfigSilently(config)
        // 更新缓存
        lock.writeLock().lock()
        try {
            cachedConfig = config
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun saveConfigSilently(config: SoundConfig) {
        try {
            val configDir = getConfigDir()
            if (!configDir.exists()) {
                configDir.mkdirs()
            }
            val configFile = getConfigFile()
            FileWriter(configFile).use { writer ->
                gson.toJson(config, writer)
            }
        } catch (e: Exception) {
            // 静默处理，无法保存配置时使用默认配置
        }
    }

    private fun createDefaultConfig(): SoundConfig {
        return SoundConfig(
            version = "0.0.1",
            enable = true,
            sounds = listOf(
                SoundMapping("build.success", "preset/build_success.wav", "构建成功"),
                SoundMapping("build.failed", "preset/build_failed.wav", "构建失败"),
                SoundMapping("run.start", "preset/run_start.wav", "运行启动"),
                SoundMapping("run.stop", "preset/run_stop.wav", "运行终止"),
                SoundMapping("compile.finished", "preset/compile_finished.wav", "编译完成"),
                SoundMapping("test.passed", "preset/test_passed.wav", "测试成功"),
                SoundMapping("test.failed", "preset/test_failed.wav", "测试失败"),
                SoundMapping("project.opened", "preset/project_opened.wav", "项目打开")
            )
        )
    }

    fun getSoundMapping(eventKey: String): SoundMapping? {
        val config = loadConfig()
        return config.sounds.find { it.eventKey == eventKey }
    }

    // 提供一个刷新缓存的方法
    fun refreshConfig() {
        lock.writeLock().lock()
        try {
            cachedConfig = null
        } finally {
            lock.writeLock().unlock()
        }
    }
}