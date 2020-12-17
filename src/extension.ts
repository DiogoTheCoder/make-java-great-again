import * as vscode from 'vscode';
import { Commands } from './commands';
import { displaySyntaxTree } from './commands/displaySyntaxTree';
import { refactorFile } from './commands/refactorFile';

export function activate(context: vscode.ExtensionContext) {
  // Use the console to output diagnostic information (console.log) and errors (console.error)
  // This line of code will only be executed once when your extension is activated
  console.log("Let's Make Java Great Again!");

  context.subscriptions.push(
    vscode.commands.registerCommand(Commands.REFACTOR_FILE, () => {
      refactorFile();
    }),
    vscode.commands.registerCommand(Commands.DISPLAY_AST, () => {
      displaySyntaxTree();
    }),
  );
}

// this method is called when your extension is deactivated
export function deactivate() {}
