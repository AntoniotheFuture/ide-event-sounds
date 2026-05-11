# IDE Event Sounds

一款跨 IDE 通用的沉浸式开发声音提示工具，为 IntelliJ 平台（IDEA、PyCharm、WebStorm 等）提供事件声音自定义功能。

## 功能特性

- ✅ 7 个核心高频事件监听（构建成功/失败、运行启动/停止、编译完成、测试通过/失败）
- ✅ 内置预设声音（可自定义替换）
- ✅ 本地配置文件持久化（JSON 格式）
- ✅ 轻量级无侵入，后台静默运行
- 🚧 自定义事件绑定（0.0.2 版本）
- 🚧 正则匹配消息内容（0.0.3 版本）

## 项目结构

```
ide-event-sounds/
├── packages/
│   ├── core/           # 公共核心模块
│   │   ├── config/     # 配置管理
│   │   ├── soundplayer/# 声音播放
│   │   └── eventmatcher/# 事件匹配
│   └── intellij/       # IntelliJ 插件模块
├── assets/             # 预设声音资源
└── docs/               # 文档
```

## 快速开始

### 环境要求

- JDK 11+
- IntelliJ IDEA 2021.1+
- Gradle 8.5+

### 构建插件

```bash
./gradlew :packages:intellij:build
```

### 运行插件

```bash
./gradlew :packages:intellij:runIde
```

### 配置说明

插件首次启动时会自动创建配置文件：
- Mac/Linux: `~/.ide-event-sounds/config.json`
- Windows: `C:\Users\用户名\.ide-event-sounds\config.json`

默认配置示例：

```json
{
  "version": "0.0.1",
  "enable": true,
  "sounds": [
    {
      "eventKey": "build.success",
      "soundPath": "preset/build_success.wav",
      "name": "构建成功",
      "regex": ""
    }
  ]
}
```

## 开发计划

- [x] 0.0.1 - 基础可用版（当前）
- [ ] 0.0.2 - 自定义扩展版
- [ ] 0.0.3 - 高级匹配版

## 许可证

MIT License
