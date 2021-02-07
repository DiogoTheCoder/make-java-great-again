import * as vscode from 'vscode';

export default () =>
  vscode.commands.executeCommand(
    'workbench.action.openSettings',
    'Make Java Great Again',
  );
