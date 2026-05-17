import * as vscode from 'vscode';
import { EventMatcher } from '../eventMatcher/eventMatcher';

export function registerFileListeners(
  context: vscode.ExtensionContext,
  eventMatcher: EventMatcher
): vscode.Disposable[] {
  const disposables: vscode.Disposable[] = [];

  disposables.push(
    vscode.workspace.onDidSaveTextDocument((doc) => {
      eventMatcher.matchAndPlay('file.saved', doc.fileName);
    })
  );

  disposables.push(
    vscode.workspace.onDidCreateFiles((e) => {
      for (const file of e.files) {
        eventMatcher.matchAndPlay('file.created', file.fsPath);
      }
    })
  );

  disposables.push(
    vscode.workspace.onDidDeleteFiles((e) => {
      for (const file of e.files) {
        eventMatcher.matchAndPlay('file.deleted', file.fsPath);
      }
    })
  );

  disposables.push(
    vscode.workspace.onDidRenameFiles((e) => {
      for (const file of e.files) {
        eventMatcher.matchAndPlay('file.renamed', file.newUri.fsPath);
      }
    })
  );

  return disposables;
}
