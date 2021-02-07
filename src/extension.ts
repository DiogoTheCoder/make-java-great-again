import * as path from 'path';
import * as vscode from 'vscode';
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
} from 'vscode-languageclient';
import { Commands } from './commands';
import { displaySyntaxTree } from './commands/displaySyntaxTree';
import { refactorFile } from './commands/refactorFile';

let client: LanguageClient;

// Name of the launcher class which contains the main.
const main: string = 'StdioLauncher';

export function activate(context: vscode.ExtensionContext) {
  // Use the console to output diagnostic information (console.log) and errors (console.error)
  // This line of code will only be executed once when your extension is activated
  console.log("Let's Make Java Great Again!");

  let classPath = path.join(
    __dirname,
    '..',
    'launcher',
    'target',
    'launcher.jar',
  );
  const args: string[] = ['-cp', classPath];

  const command = 'java';

  // Set the server options
  // -- java execution path
  // -- argument to be pass when executing the Java command
  let serverOptions: ServerOptions = {
    command,
    args: [...args, main],
    options: {},
  };

  let clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: 'file', language: 'java' }],
    synchronize: {
      configurationSection: 'java',
    },
  };

  client = new LanguageClient(
    'MJGA',
    'Make Java Great Again',
    serverOptions,
    clientOptions,
  );
  let disposable = client.start();

  context.subscriptions.push(
    disposable,
    vscode.commands.registerCommand(Commands.REFACTOR_FILE, () => {
      refactorFile(client);
    }),
    vscode.commands.registerCommand(Commands.DISPLAY_AST, () => {
      displaySyntaxTree();
    }),
  );
}

export function deactivate() {
  console.log('Thank you for using Make Java Great Again! :)');
  if (client) {
    client.stop();
  }
}
