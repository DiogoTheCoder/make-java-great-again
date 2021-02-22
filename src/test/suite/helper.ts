import * as vscode from 'vscode';
import path from 'path';

export let doc: vscode.TextDocument;
export let editor: vscode.TextEditor;

export async function activate(filePath: string) {
  const ext = vscode.extensions.getExtension(
    'DiogoTheCoder.make-java-great-again',
  )!;

  await ext.activate();
  try {
    doc = await vscode.workspace.openTextDocument(filePath);
    editor = await vscode.window.showTextDocument(doc);
  } catch (e) {
    console.error(e);
    throw Error(e);
  }
}

export function getDocPath(relativePath: string) {
  // Resolve Windows nonsense
  relativePath = relativePath.replace('%3A', '');
  return path.join(__dirname, `test/src/${relativePath}`);
}

export async function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
