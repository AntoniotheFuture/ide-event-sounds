import groovy.json.JsonSlurper

plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("org.jetbrains.intellij") version "1.17.3" apply false
}

// 读取统一配置文件
val configFile = file("project.config.json")
val config = if (configFile.exists()) {
    JsonSlurper().parse(configFile) as Map<String, Any>
} else {
    throw GradleException("项目配置文件 project.config.json 不存在")
}

// 从配置文件提取项目信息
val projectConfig = config["project"] as Map<String, Any>
val authorConfig = config["author"] as Map<String, Any>
val packagesConfig = config["packages"] as Map<String, Any>

allprojects {
    repositories {
        mavenCentral()
    }
}

ext {
    // 项目基础配置
    set("projectName", projectConfig["name"] as String)
    set("projectDescription", projectConfig["description"] as String)
    set("projectGroup", projectConfig["groupId"] as String)
    set("projectVersion", projectConfig["version"] as String)
    
    // 作者信息
    set("authorName", authorConfig["name"] as String)
    set("authorEmail", authorConfig["email"] as String)
    set("githubUrl", authorConfig["github"] as String)
    
    // 包名配置
    val intellijPackage = packagesConfig["intellij"] as Map<String, Any>
    set("intellijPluginId", intellijPackage["pluginId"] as String)
    set("intellijPackageName", intellijPackage["packageName"] as String)
    
    // 依赖版本
    val dependencies = config["dependencies"] as Map<String, Any>
    set("kotlinVersion", dependencies["kotlinVersion"] as String)
    set("intellijSdkVersion", dependencies["intellijSdkVersion"] as String)
    set("javaVersion", dependencies["javaVersion"] as String)
    
    // 配置文件路径
    set("projectConfigFile", configFile.absolutePath)
}

// 任务：打印当前配置
tasks.register("printConfig") {
    doLast {
        println("=== 项目配置 ===")
        println("项目名称: ${ext["projectName"]}")
        println("项目版本: ${ext["projectVersion"]}")
        println("作者: ${ext["authorName"]} <${ext["authorEmail"]}>")
        println("IntelliJ 插件ID: ${ext["intellijPluginId"]}")
        println("===============")
    }
}