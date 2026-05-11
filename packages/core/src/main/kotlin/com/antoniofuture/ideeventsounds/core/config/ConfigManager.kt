package com.antoniofuture.ideeventsounds.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun getConfigDir(): File {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".ide-event-sounds")
    }

    fun getConfigFile(): File {
        return File(getConfigDir(), "config.json")
    }

    fun loadConfig(): SoundConfig {
        val configFile = getConfigFile()
        return if (configFile.exists()) {
            try {
                FileReader(configFile).use { reader ->
                    val config = gson.fromJson(reader, SoundConfig::class.java)
                    return upgradeConfig(config)
                }
            } catch (e: Exception) {
                createDefaultConfig()
            }
        } else {
            createDefaultConfig()
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
}