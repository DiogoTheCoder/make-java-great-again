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

  const refactorConfig = vscode.workspace
    .getConfiguration('java')
    .get('refactor') as any;

  let options = {
    reduce: { operators: refactorConfig.reduce.operators ?? [] },
  };

  let transformedCode = await vscode.commands.executeCommand(
    'mjga.langserver.refactorFile',
    activeTextEditor.document.uri.fsPath,
    options,
  );

  if (typeof transformedCode === 'string') {
    writeCode(transformedCode);
  }
}
