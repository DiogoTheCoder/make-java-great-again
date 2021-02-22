import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import path from 'path';
// import * as myExtension from '../../extension';

suite('Extension Test Suite', () => {
  vscode.window.showInformationMessage('Start all tests.');

  test('Loading Java file', async () => {
    let filePath = path.join(__dirname, 'test/src/App.java');
    filePath = filePath.replace('%3A', '');
    const textDocument = await vscode.workspace.openTextDocument(filePath);

    assert.strictEqual(
      textDocument.getText().length > 0,
      true,
      'Java file contains code',
    );
  });
});
