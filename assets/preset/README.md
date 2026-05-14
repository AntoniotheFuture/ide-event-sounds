# 预设声音目录

请将以下 WAV 格式声音文件放置在此目录：

- `build_success.wav` - 构建成功提示音
- `build_failed.wav` - 构建失败提示音
- `run_start.wav` - 运行启动提示音
- `run_stop.wav` - 运行终止提示音
- `compile_finished.wav` - 编译完成提示音
- `test_passed.wav` - 测试成功提示音
- `test_failed.wav` - 测试失败提示音
- `project_opened.wav` - 项目打开提示音

## 声音文件要求

- 格式：WAV（推荐 16-bit PCM）
- 时长：1-3 秒
- 采样率：44.1kHz 或 48kHz

## 临时占位方案

在添加真实声音文件前，你可以：
1. 使用在线 TTS 工具生成语音（如 Microsoft Azure TTS、Google TTS）
2. 从免费音效库下载（如 Freesound.org）
3. 使用系统自带的提示音

## 打包说明

声音文件会被插件自动复制到 `~/.ide-event-sounds/preset/` 目录。

## 自定义声音文件（0.0.2+）

除了使用 preset 目录下的预设声音，你还可以使用本地自定义音频文件：

### 支持的路径格式

```json
{
  "eventKey": "build.success",
  "soundPath": "/Users/username/Documents/my-sounds/build.wav",
  "name": "我的构建成功音",
  "isCustom": true,
  "isEnabled": true
}
```

### 支持的格式
- WAV（推荐）
- MP3

### 注意事项
- 建议使用绝对路径
- 确保文件存在且可读
- 路径中的特殊字符需要转义