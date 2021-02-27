import * as vscode from 'vscode';
import { Extension, TextDocument } from 'vscode';
import path from 'path';

export let doc: vscode.TextDocument;
export let editor: vscode.TextEditor;

export async function activate(
  extension: Extension<any>,
  filePath: string,
): Promise<TextDocument> {
  await extension.activate();
  try {
    doc = await vscode.workspace.openTextDocument(filePath);
    editor = await vscode.window.showTextDocument(doc);
  } catch (e) {
    console.error(e);
    throw Error(e);
  }

  return doc;
}

export function getDocPath(relativePath: string) {
  // Resolve Windows nonsense
  relativePath = relativePath.replace('%3A', '');
  return path.join(__dirname, `java/src/${relativePath}`);
}

export async function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
