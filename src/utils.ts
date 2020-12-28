import * as prettierJava from 'prettier-plugin-java';
import * as vscode from 'vscode';
import { CstNode } from 'chevrotain';

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

export function parse(code: string): CstNode {
  return prettierJava.parsers.java.parse(code, null, 'ordinaryCompilationUnit');
}

export function writeCode(code: string): void {
  const editor = getEditor();
  const edit = new vscode.WorkspaceEdit();

  const wholeDocument = new vscode.Range(
    new vscode.Position(0, 0),
    new vscode.Position(editor.document.lineCount, 0),
  );

  const updateCode = new vscode.TextEdit(wholeDocument, code);
  edit.set(editor.document.uri, [updateCode]);

  vscode.workspace.applyEdit(edit);
}
