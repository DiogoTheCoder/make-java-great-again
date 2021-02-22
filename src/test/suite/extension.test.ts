import * as assert from 'assert';
import * as vscode from 'vscode';
import { activate, getDocPath, sleep } from './helper';
import { Commands } from '../../commands';

const hex = require('string-hex');

suite('Extension Test Suite', () => {
  vscode.window.showInformationMessage('Start all tests.');

  const FILE_PATH = getDocPath('App.java');
  test('Loading Java file', async () => {
    const textDocument = await vscode.workspace.openTextDocument(FILE_PATH);

    assert.strictEqual(textDocument.getText().length > 0, true);
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

  test('Running Refactor Command', async () => {
    const textDocument = await vscode.workspace.openTextDocument(FILE_PATH);

    // Run the refactor command, wait for reply back, then save
    await vscode.commands.executeCommand(Commands.REFACTOR_FILE);
    await sleep(2000);
    await textDocument.save();

    const refactoredCode = textDocument.getText().trim();

    // Simpler to compare hex values instead of the entire file
    const refactoredCodeHex = hex(refactoredCode);
    assert.strictEqual(
      refactoredCodeHex,
      '7075626c696320636c61737320417070207baa202020207075626c69632073746174696320766f6964206d61696e28537472696e675b5d206172677329207468726f777320457863657074696f6e207ba20202020202020204c6973743c537472696e673e206e616d6573203d204172726179732e61734c697374282244696f676f222c2022436f73746122293ba20202020202020206e616d65732e666f724561636828a202020202020202020202020737472696e67202d3e207ba2020202020202020202020202020202053797374656d2e6f75742e7072696e746c6e28737472696e67293ba2020202020202020202020207da2020202020202020293ba202020207da7d',
    );
  });
});
