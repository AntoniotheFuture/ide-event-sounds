import org.apache.tools.ant.taskdefs.Zip
import org.apache.tools.ant.types.FileSet

plugins {
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm")
}

// 直接使用根模块已设置的配置
group = rootProject.ext["projectGroup"] as String
version = rootProject.ext["projectVersion"] as String

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":packages:core"))
}

intellij {
    version.set("2023.3")
    type.set("IC")
    plugins.set(listOf())
    
    // 设置插件构建输出文件名
    pluginName.set("ide-event-sounds")
}

tasks {
    patchPluginXml {
        // 动态设置插件 ID
        pluginId.set(rootProject.ext["intellijPluginId"] as String)
        
        // 动态设置版本
        version.set(project.version.toString())
        
        // 动态设置描述
        pluginDescription.set("""
            <h2>${rootProject.ext["projectName"]}</h2>
            <p>${rootProject.ext["projectDescription"]}</p>
            <ul>
                <li>构建成功/失败提示音</li>
                <li>运行启动/终止提示音</li>
                <li>编译完成提示音</li>
                <li>测试通过/失败提示音</li>
            </ul>
        """.trimIndent())
        
        // 动态设置变更日志
        changeNotes.set("""
            <h3>${project.version}</h3>
            <ul>
                <li>新增项目打开事件提示音</li>
                <li>修复声音播放问题（clip.open 缺失）</li>
                <li>修复沙箱权限问题（直接读取资源文件）</li>
                <li>添加 BufferedInputStream 支持 mark/reset</li>
                <li>优化事件匹配逻辑，避免重复触发</li>
                <li>支持新语音播放时停止当前播放</li>
                <li>添加 GitHub Actions 自动构建和发布</li>
            </ul>
            <h3>0.0.1</h3>
            <ul>
                <li>初始版本发布</li>
                <li>支持 7 个核心高频事件</li>
                <li>内置预设声音</li>
            </ul>
        """.trimIndent())
        
        sinceBuild.set("233")
        untilBuild.set("999.*")
    }
    
    runIde {
        jvmArgs = listOf("-Dproject.config=${rootProject.ext["projectConfigFile"]}")
    }
    
    // 在 buildPlugin 任务完成后合并 core 模块的类
    buildPlugin {
        doLast {
            val libsDir = file("build/libs")
            val intellijJar = libsDir.listFiles { _, name -> name.startsWith("intellij-") && name.endsWith(".jar") && !name.startsWith("instrumented-") }?.first()
            val coreJar = rootProject.file("packages/core/build/libs/core.jar")
            
            if (intellijJar != null && coreJar.exists()) {
                val newFileName = "ide-event-sounds-${project.version}.jar"
                val newFile = libsDir.resolve(newFileName)
                
                // 使用 shell 命令合并 jar 文件
                project.exec {
                    commandLine("bash", "-c", """
                        cd "${libsDir.absolutePath}"
                        unzip -q "${intellijJar.absolutePath}" -d temp_plugin
                        unzip -q "${coreJar.absolutePath}" -d temp_plugin
                        cd temp_plugin
                        jar cf "../${newFileName}" .
                        cd ..
                        rm -rf temp_plugin
                    """.trimIndent())
                }
                
                // 删除旧文件
                intellijJar.delete()
                
                println("Created plugin file: $newFileName with core module included")
            }
        }
    }
}