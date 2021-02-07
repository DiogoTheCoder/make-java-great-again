import * as vscode from 'vscode';
import { getEditor, writeCode } from '../utils';
import { LanguageClient } from 'vscode-languageclient';

export async function refactorFile(client: LanguageClient): Promise<void> {
  const activeTextEditor = getEditor();
  if (!activeTextEditor) {
    throw Error('No active text editor');
  }

  if (!activeTextEditor.document) {
    throw Error('No active Java file');
  }

  let transformedCode = await vscode.commands.executeCommand(
    'mjga.langserver.refactorFile',
    activeTextEditor.document.uri.fsPath,
  );
  if (typeof transformedCode === 'string') {
    writeCode(transformedCode);
  }
}
