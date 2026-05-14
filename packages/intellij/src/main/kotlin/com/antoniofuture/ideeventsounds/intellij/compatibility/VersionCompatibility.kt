package com.antoniofuture.ideeventsounds.intellij.compatibility

import com.intellij.openapi.application.ApplicationInfo

object VersionCompatibility {
    private val version: Int by lazy {
        try {
            // 完全反射方式获取版本号
            val applicationInfo = ApplicationInfo.getInstance()
            val buildObj = applicationInfo.javaClass.getMethod("getBuild").invoke(applicationInfo)
            
            // 尝试 asInt()
            try {
                buildObj.javaClass.getMethod("asInt").invoke(buildObj) as Int
            } catch (e: Exception) {
                // 尝试 getBuildNumber()
                try {
                    val buildNumber = buildObj.javaClass.getMethod("getBuildNumber").invoke(buildObj)
                    buildNumber.toString().split(".").firstOrNull()?.toInt() ?: 211
                } catch (e2: Exception) {
                    211
                }
            }
        } catch (e: Exception) {
            211
        }
    }

    // IntelliJ 版本常量
    const val VERSION_2021_1 = 211
    const val VERSION_2021_2 = 212
    const val VERSION_2021_3 = 213
    const val VERSION_2022_1 = 221
    const val VERSION_2022_2 = 222
    const val VERSION_2022_3 = 223
    const val VERSION_2023_1 = 231
    const val VERSION_2023_2 = 232
    const val VERSION_2023_3 = 233

    // 检查当前版本是否 >= 指定版本
    fun isAtLeast(versionCode: Int): Boolean {
        return version >= versionCode
    }

    // 检查当前版本是否在范围内
    fun isBetween(minVersion: Int, maxVersion: Int): Boolean {
        return version >= minVersion && version <= maxVersion
    }

    // 获取当前版本字符串
    fun getVersionString(): String {
        try {
            val applicationInfo = ApplicationInfo.getInstance()
            val buildObj = applicationInfo.javaClass.getMethod("getBuild").invoke(applicationInfo)
            return buildObj.toString()
        } catch (e: Exception) {
            return "unknown"
        }
    }

    // 获取构建号
    fun getBuildNumber(): Int {
        return version
    }

    // 判断是否支持 GitCommandListener API
    fun supportsGitCommandListener(): Boolean {
        return isAtLeast(VERSION_2021_1)
    }

    // 判断是否支持新的 ApplicationLifecycleListener API
    fun supportsApplicationLifecycleListener(): Boolean {
        return isAtLeast(VERSION_2022_1)
    }

    // 判断是否支持 ProjectActivity API
    fun supportsProjectActivity(): Boolean {
        return isAtLeast(VERSION_2021_2)
    }
}