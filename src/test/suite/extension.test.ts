import * as assert from 'assert';
import * as vscode from 'vscode';
import { activate, getDocPath } from './helper';
import { Done } from 'mocha';
import { strict, strictEqual } from 'assert';

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
    // const ext = vscode.extensions.getExtension(
    //   'DiogoTheCoder.make-java-great-again',
    // )!;

    await activate(FILE_PATH);

    // assert.strictEqual(
    //   ext.isActive,
    //   true,
    //   'Extension has been activated successfully',
    // );
  });
});
