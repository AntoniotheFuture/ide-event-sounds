import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { EventMatcher } from './eventMatcher/eventMatcher';
import { ConfigManager } from './config/configManager';
import { registerFileListeners } from './listeners/fileListener';
import { registerTaskListeners } from './listeners/taskListener';
import { registerGitListeners } from './listeners/gitListener';

let eventMatcher: EventMatcher;
let configManager: ConfigManager;
let allDisposables: vscode.Disposable[] = [];

export function activate(context: vscode.ExtensionContext) {
  console.log('[IDE Event Sounds] Activating...');

  configManager = ConfigManager.getInstance();
  configManager.setResourcePresetDir(path.join(context.extensionPath, 'resources', 'preset'));
  eventMatcher = new EventMatcher();

  allDisposables = [
    ...registerFileListeners(context, eventMatcher),
    ...registerTaskListeners(context, eventMatcher),
    ...registerGitListeners(context, eventMatcher),
  ];

  context.subscriptions.push(
    vscode.commands.registerCommand('ide-event-sounds.openConfig', () => {
      openConfigFile();
    })
  );

  context.subscriptions.push(
    vscode.commands.registerCommand('ide-event-sounds.testNotification', () => {
      triggerTestNotification();
    })
  );

  context.subscriptions.push(
    vscode.commands.registerCommand('ide-event-sounds.toggleEnable', () => {
      toggleEnable();
    })
  );

  context.subscriptions.push(...allDisposables);

  eventMatcher.matchAndPlay('project.opened');

  vscode.window.showInformationMessage('IDE Event Sounds 已激活！');

  console.log('[IDE Event Sounds] Activated successfully');
}

export function deactivate() {
  console.log('[IDE Event Sounds] Deactivating...');

  for (const disposable of allDisposables) {
    disposable.dispose();
  }
  allDisposables = [];
}

function openConfigFile(): void {
  const configPath = configManager.getConfigPath();

  if (!fs.existsSync(configPath)) {
    configManager.loadConfig();
  }

  vscode.commands.executeCommand('vscode.open', vscode.Uri.file(configPath));
}

function triggerTestNotification(): void {
  eventMatcher.matchAndPlay('notification', 'IDE Event Sounds 测试通知');
  vscode.window.showInformationMessage('已发送测试通知');
}

function toggleEnable(): void {
  const config = configManager.loadConfig();
  config.enable = !config.enable;
  configManager.saveConfig(config);

  const status = config.enable ? '已启用' : '已禁用';
  vscode.window.showInformationMessage(`事件声音${status}`);
}
