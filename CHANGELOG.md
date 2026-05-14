# Change Log

## 0.0.2 (2026-05-12)

### 新增功能
- 支持自定义事件绑定（用户可通过事件 Key 添加自定义 IDE 原生事件）
- 支持编辑事件绑定的声音（可替换预设声音为本地自定义音频文件）
- 支持删除自定义事件
- 支持启用/禁用单个事件
- 支持自定义本地音频文件路径

### 改进
- 配置版本升级到 0.0.2
- 新增 `isCustom` 和 `isEnabled` 字段支持
- 新增 `getCustomEvents()` 和 `getPresetEvents()` API
- 新增 `getAllAvailableEventKeys()` 提供可用事件 Key 列表
- 新增 `setPluginEnabled()` 全局开关控制

### Bug 修复
- 修复声音播放问题（clip.open 缺失）
- 修复沙箱权限问题（直接读取资源文件）
- 添加 BufferedInputStream 支持 mark/reset
- 优化事件匹配逻辑，避免重复触发
- 支持新语音播放时停止当前播放

### 构建
- 添加 GitHub Actions 自动构建和发布

## 0.0.1
- 初始版本发布
- 支持 7 个核心高频事件
- 内置预设声音