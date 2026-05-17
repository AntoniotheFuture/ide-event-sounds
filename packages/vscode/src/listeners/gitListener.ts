import * as vscode from 'vscode';
import { EventMatcher } from '../eventMatcher/eventMatcher';

export function registerGitListeners(
  context: vscode.ExtensionContext,
  eventMatcher: EventMatcher
): vscode.Disposable[] {
  const disposables: vscode.Disposable[] = [];
  const gitExtension = vscode.extensions.getExtension('vscode.git');

  if (!gitExtension || !gitExtension.isActive) {
    console.log('[GitListener] Git extension not available');
    return disposables;
  }

  const gitApi = gitExtension.exports.getAPI(1);

  if (!gitApi) {
    console.log('[GitListener] Git API not available');
    return disposables;
  }

  gitApi.onDidOpenRepository((repo: any) => {
    if (repo && repo.state) {
      repo.state.onDidChange(() => {
        // 检测 HEAD 变化用于分支切换检测
        const head = repo.state.HEAD;
        if (head && head.name) {
          eventMatcher.matchAndPlay('git.branch.checkedout', head.name);
        }
      });
    }
  });

  return disposables;
}
