# 事件 Key 速查表

> **说明**：✅ 表示已实现，🔄 表示开发中，❌ 表示尚未实现

## 核心高频事件

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `build.success` | 构建成功 | ✅ | ✅ |
| `build.failed` | 构建失败 | ✅ | ✅ |
| `run.start` | 应用启动 | ✅ | ✅ |
| `run.stop` | 应用停止 | ✅ | ✅ |
| `test.passed` | 测试通过 | ✅ | ✅ |
| `test.failed` | 测试失败 | ✅ | ✅ |

---

## 项目和应用生命周期

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `project.opened` | 项目打开 | ❌ | ✅ |
| `project.closed` | 项目关闭 | ❌ | ❌ |
| `application.frame.created` | 应用窗口创建 | ❌ | ✅ |
| `application.started` | 应用启动完成 | ❌ | ✅ |
| `application.closing` | 应用关闭中 | ❌ | ✅ |

---

## 调试和编译

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `debug.started` | 调试会话开始 | ❌ | ✅ |
| `debug.stopped` | 调试会话停止 | ❌ | ✅ |
| `compile.finished` | 编译完成 | ❌ | ❌ |

---

## 索引和代码检查

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `indexing.started` | 索引开始 | ❌ | ✅ |
| `indexing.finished` | 索引完成 | ❌ | ✅ |
| `inspection.finished.clean` | 代码检查（无问题） | ❌ | ❌ |
| `inspection.finished.withIssues` | 代码检查（有问题） | ❌ | ❌ |

---

## 文件操作

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `file.created` | 文件创建 | ❌ | ✅ |
| `file.deleted` | 文件删除 | ❌ | ✅ |
| `file.saved` | 文件保存 | ❌ | ✅ |
| `file.renamed` | 文件重命名 | ❌ | ✅ |
| `file.moved` | 文件移动 | ❌ | ❌ |
| `file.copied` | 文件复制 | ❌ | ❌ |

---

## Git 操作

> **说明**：Git 事件使用反射方式实现版本兼容，支持 COMMIT、PUSH、PULL、CHECKOUT、MERGE、REBASE 等命令
> 
> **事件Key格式**：`git.{COMMAND}.{status}`，其中 COMMAND 为大写（如 PULL、PUSH），status 为 started/success/failed

### 通用命令事件

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `git.{COMMAND}.started` | Git命令开始 | ❌ | ✅ |
| `git.{COMMAND}.success` | Git命令成功 | ❌ | ✅ |
| `git.{COMMAND}.failed` | Git命令失败 | ❌ | ✅ |

**支持的命令**：PULL, PUSH, COMMIT, MERGE, REBASE, CHECKOUT, FETCH, CLONE, BRANCH, STASH 等

### 特殊事件

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `git.branch.checkedout` | 切换分支成功 | ❌ | ✅ |

### 分支相关（未实现）

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `git.branch.created` | 分支创建 | ❌ | ❌ |
| `git.branch.deleted` | 分支删除 | ❌ | ❌ |
| `git.branch.merged` | 分支合并 | ❌ | ❌ |

### Stash 操作（未实现）

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `git.stash.created` | 创建 Stash | ❌ | ❌ |
| `git.stash.popped` | 应用 Stash | ❌ | ❌ |
| `git.stash.dropped` | 删除 Stash | ❌ | ❌ |

### 冲突（未实现）

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `git.conflict.resolved` | 冲突解决 | ❌ | ❌ |

---

## 项目同步

| Event Key | 事件描述 | 默认启用 | 已实现 |
|-----------|----------|----------|--------|
| `sync.started` | 项目同步开始 | ❌ | ❌ |
| `sync.finished` | 项目同步完成 | ❌ | ❌ |
| `sync.failed` | 项目同步失败 | ❌ | ❌ |

---

## 使用示例

在 `~/.ide-event-sounds/config.json` 中添加自定义事件：

```json
{
  "version": "0.0.2",
  "enable": true,
  "sounds": [
    {
      "eventKey": "git.COMMIT.success",
      "soundPath": "/Users/yourname/sounds/commit.mp3",
      "name": "Git 提交成功",
      "regex": "",
      "isCustom": true,
      "isEnabled": true
    },
    {
      "eventKey": "file.saved",
      "soundPath": "/Users/yourname/sounds/save.wav",
      "name": "文件保存",
      "isCustom": true,
      "isEnabled": true
    }
  ]
}
```

**注意**：Git 事件Key中的命令名必须大写，例如 `git.PULL.success` 而不是 `git.pull.success`。
