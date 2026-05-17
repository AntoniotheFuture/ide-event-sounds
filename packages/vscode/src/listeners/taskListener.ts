import * as vscode from 'vscode';
import { EventMatcher } from '../eventMatcher/eventMatcher';

export function registerTaskListeners(
  context: vscode.ExtensionContext,
  eventMatcher: EventMatcher
): vscode.Disposable[] {
  const disposables: vscode.Disposable[] = [];

  disposables.push(
    vscode.tasks.onDidStartTask((e) => {
      const taskName = e.execution.task.name.toLowerCase();

      if (taskName.includes('build')) {
        eventMatcher.matchAndPlay('build.started', taskName);
      } else if (taskName.includes('compile')) {
        eventMatcher.matchAndPlay('compile.started', taskName);
      } else if (taskName.includes('run') || taskName.includes('start')) {
        eventMatcher.matchAndPlay('run.start', taskName);
      } else if (taskName.includes('debug')) {
        eventMatcher.matchAndPlay('debug.started', taskName);
      } else if (taskName.includes('test')) {
        eventMatcher.matchAndPlay('test.started', taskName);
      }
    })
  );

  disposables.push(
    vscode.tasks.onDidEndTask((e) => {
      const taskName = e.execution.task.name.toLowerCase();

      if (taskName.includes('build')) {
        eventMatcher.matchAndPlay('build.success', taskName);
      } else if (taskName.includes('compile')) {
        eventMatcher.matchAndPlay('compile.finished', taskName);
      } else if (taskName.includes('run') || taskName.includes('start')) {
        eventMatcher.matchAndPlay('run.stop', taskName);
      } else if (taskName.includes('debug')) {
        eventMatcher.matchAndPlay('debug.stopped', taskName);
      } else if (taskName.includes('test')) {
        eventMatcher.matchAndPlay('test.passed', taskName);
      }
    })
  );

  return disposables;
}
