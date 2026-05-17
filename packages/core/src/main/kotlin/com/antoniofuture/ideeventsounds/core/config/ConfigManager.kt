package com.antoniofuture.ideeventsounds.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantReadWriteLock

class ConfigManager private constructor() {
    companion object {
        val instance: ConfigManager by lazy { ConfigManager() }
    }
    
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
        // 版本升级逻辑
        var config = existingConfig
        
        // 从 0.0.1 升级到 0.0.2
        if (config.version == "0.0.1") {
            config = upgradeTo002(config)
        }
        
        // 从 0.0.2 升级到 0.0.3
        if (config.version == "0.0.2") {
            config = upgradeTo003(config)
        }
        
        // 添加缺失的默认事件
        val defaultSounds = listOf(
            SoundMapping("build.success", "preset/build_success.wav", "构建成功"),
            SoundMapping("build.failed", "preset/build_failed.wav", "构建失败"),
            SoundMapping("run.start", "preset/run_start.wav", "运行启动"),
            SoundMapping("run.stop", "preset/run_stop.wav", "运行终止"),
            SoundMapping("compile.finished", "preset/compile_finished.wav", "编译完成"),
            SoundMapping("test.passed", "preset/test_passed.wav", "测试成功"),
            SoundMapping("test.failed", "preset/test_failed.wav", "测试失败"),
            SoundMapping("project.opened", "preset/project_opened.wav", "项目打开"),
            SoundMapping("debug.started", "preset/debug_started.wav", "调试开始"),
            SoundMapping("debug.stopped", "preset/debug_stopped.wav", "调试停止"),
            SoundMapping("file.created", "preset/file_created.wav", "文件创建"),
            SoundMapping("file.deleted", "preset/file_deleted.wav", "文件删除"),
            SoundMapping("file.saved", "preset/file_saved.wav", "文件保存"),
            SoundMapping("file.renamed", "preset/file_renamed.wav", "文件重命名"),
            SoundMapping("git.COMMIT.success", "preset/git_commit.wav", "Git提交成功"),
            SoundMapping("git.PULL.success", "preset/git_pull.wav", "Git拉取成功"),
            SoundMapping("git.PUSH.success", "preset/git_push.wav", "Git推送成功")
        )

        val existingKeys = config.sounds.map { it.eventKey }.toSet()
        val missingSounds = defaultSounds.filter { !existingKeys.contains(it.eventKey) }

        if (missingSounds.isNotEmpty()) {
            val updatedSounds = config.sounds + missingSounds
            config = SoundConfig(
                version = config.version,
                enable = config.enable,
                sounds = updatedSounds
            )
        }

        if (config.version != "0.0.3") {
            config = SoundConfig(
                version = "0.0.3",
                enable = config.enable,
                sounds = config.sounds
            )
            saveConfigSilently(config)
        }

        return config
    }

    private fun upgradeTo002(config: SoundConfig): SoundConfig {
        return SoundConfig(
            version = "0.0.2",
            enable = config.enable,
            sounds = config.sounds
        )
    }

    private fun upgradeTo003(config: SoundConfig): SoundConfig {
        // 添加regex字段支持（默认空字符串）
        val updatedSounds = config.sounds.map {
            if (it.regex.isEmpty()) it else it
        }
        return SoundConfig(
            version = "0.0.3",
            enable = config.enable,
            sounds = updatedSounds
        )
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
            version = "0.0.3",
            enable = true,
            sounds = listOf(
                SoundMapping("build.success", "preset/build_success.wav", "构建成功"),
                SoundMapping("build.failed", "preset/build_failed.wav", "构建失败"),
                SoundMapping("run.start", "preset/run_start.wav", "运行启动"),
                SoundMapping("run.stop", "preset/run_stop.wav", "运行终止"),
                SoundMapping("compile.finished", "preset/compile_finished.wav", "编译完成"),
                SoundMapping("test.passed", "preset/test_passed.wav", "测试成功"),
                SoundMapping("test.failed", "preset/test_failed.wav", "测试失败"),
                SoundMapping("project.opened", "preset/project_opened.wav", "项目打开"),
                SoundMapping("debug.started", "preset/debug_started.wav", "调试开始"),
                SoundMapping("debug.stopped", "preset/debug_stopped.wav", "调试停止"),
                SoundMapping("file.created", "preset/file_created.wav", "文件创建"),
                SoundMapping("file.deleted", "preset/file_deleted.wav", "文件删除"),
                SoundMapping("file.saved", "preset/file_saved.wav", "文件保存"),
                SoundMapping("file.renamed", "preset/file_renamed.wav", "文件重命名"),
                SoundMapping("git.COMMIT.success", "preset/git_commit.wav", "Git提交成功"),
                SoundMapping("git.PULL.success", "preset/git_pull.wav", "Git拉取成功"),
                SoundMapping("git.PUSH.success", "preset/git_push.wav", "Git推送成功"),
                // 带正则匹配的示例事件
                SoundMapping("build.failed.error", "preset/build_failed_error.wav", "构建失败（含错误）", "error|Error|ERROR"),
                SoundMapping("test.failed.assertion", "preset/test_failed_assert.wav", "测试失败（断言错误）", "AssertionError|assert")
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