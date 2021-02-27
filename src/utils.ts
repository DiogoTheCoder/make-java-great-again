import * as prettier from 'prettier';
import * as vscode from 'vscode';

export function readCode(): string {
  const editor = getEditor();
  return editor.document.getText();
}

export function getEditor(): vscode.TextEditor {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    throw Error('No active editor!');
  }

  return editor;
}

export function writeCode(code: string): void {
  const editor = getEditor();
  const edit = new vscode.WorkspaceEdit();

  const wholeDocument = new vscode.Range(
    new vscode.Position(0, 0),
    new vscode.Position(editor.document.lineCount, 0),
  );

  const formattedCode = prettier.format(code, {
    parser: 'java',
    tabWidth: Number.parseInt(editor.options.tabSize?.toString() ?? '4', 10),
  });

  const updateCode = new vscode.TextEdit(wholeDocument, formattedCode);
  edit.set(editor.document.uri, [updateCode]);

  vscode.workspace.applyEdit(edit);
}
