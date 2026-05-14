# 调试配置

本目录用于本地调试插件，包含所有支持事件的配置文件和声音文件占位符。

## 目录结构

```
debug/
├── config.json          # 调试用配置文件
└── sounds/              # 调试用声音文件目录
    ├── build_success.wav
    ├── build_failed.wav
    ├── run_start.wav
    ├── run_stop.wav
    ├── test_passed.wav
    ├── test_failed.wav
    ├── debug_started.wav
    ├── debug_stopped.wav
    ├── compile_finished.wav
    ├── project_opened.wav
    ├── application_starting.wav
    ├── application_loaded.wav
    ├── frame_created.wav
    ├── indexing_started.wav
    ├── indexing_finished.wav
    ├── file_created.wav
    ├── file_deleted.wav
    ├── file_moved.wav
    ├── file_renamed.wav
    ├── file_saved.wav
    ├── git_commit_success.wav
    ├── git_commit_failed.wav
    ├── git_push_success.wav
    ├── git_push_failed.wav
    ├── git_pull_success.wav
    ├── git_pull_failed.wav
    ├── git_branch_checkedout.wav
    ├── git_merge_success.wav
    ├── git_merge_failed.wav
    ├── git_rebase_success.wav
    └── git_rebase_failed.wav
```

## 使用方法

### 1. 添加声音文件

将对应的 `.wav` 格式声音文件放入 `sounds/` 目录，文件名需与 `config.json` 中的 `soundPath` 一致。

### 2. 启动调试

```bash
./gradlew :packages:intellij:runIde
```

调试模式会自动使用 `debug/config.json` 配置文件。

### 3. 发布构建

```bash
./gradlew :packages:intellij:assemble
```

发布时会自动使用 `preset/config.example.json` 配置文件。

## 支持的事件列表

详细事件列表请参考 [docs/EVENT_KEYS.md](./EVENT_KEYS.md)

## 注意事项

- 调试配置仅在本地开发调试时使用
- 发布时会自动切换到预设配置
- 声音文件建议使用 `.wav` 格式以确保兼容性
