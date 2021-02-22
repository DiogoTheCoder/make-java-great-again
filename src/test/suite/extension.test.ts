import * as assert from 'assert';
import * as vscode from 'vscode';
import { activate, getDocPath, sleep } from './helper';

suite('Extension Test Suite', () => {
  vscode.window.showInformationMessage('Start all tests.');

  const FILE_PATH = getDocPath('App.java');
  test('Loading Java file', async () => {
    const textDocument = await vscode.workspace.openTextDocument(FILE_PATH);

    assert.strictEqual(
      textDocument.getText().length > 0,
      true,
      'Java file contains code',
    );
  });

  test('Connecting to Language Server', async () => {
    const ext = vscode.extensions.getExtension(
      'DiogoTheCoder.make-java-great-again',
    )!;

    await activate(FILE_PATH);

    assert.strictEqual(ext.isActive, true);

    await sleep(2000);

    const textDocument = await vscode.workspace.openTextDocument(FILE_PATH);
    const diagnostics = vscode.languages.getDiagnostics(textDocument.uri);
    assert.strictEqual(diagnostics.length, 1);

    const mjgaDiagnostic = diagnostics[0];
    assert.strictEqual(mjgaDiagnostic.source, 'Make Java Great Again');
    assert.strictEqual(mjgaDiagnostic.code, 'forEach');
    assert.strictEqual(
      mjgaDiagnostic.message,
      'Can refactor this into a forEach',
    );
    assert.strictEqual(mjgaDiagnostic.severity, 2);
    assert.strictEqual(mjgaDiagnostic.range.start.line, 5 - 1);
    assert.strictEqual(mjgaDiagnostic.range.end.line, 7 - 1);
  });
});
