import * as vscode from 'vscode';
import { TextEditor } from 'vscode';

export function activate(context: vscode.ExtensionContext) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Let\'s Make Java Great Again!');

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with registerCommand
	// The commandId parameter must match the command field in package.json
	let disposable = vscode.commands.registerCommand('make-java-great-again.refactorFile', () => {
		const code = readCode();
		if (typeof code !== 'string') {
			throw Error('Was unable to read code!');
		}

		const transformedCode = transformCode(code);
		writeCode(transformedCode);
	});

	context.subscriptions.push(disposable);
}


function transformCode(code: string): string {
	return `${code}
// Transformed from Make Java Great Again`;
}

function readCode(): string {
	const editor = getEditor();
	return editor.document.getText();
}

function writeCode(code: string): void {
	const editor = getEditor();
	const edit = new vscode.WorkspaceEdit();

	const wholeDocument = new vscode.Range(
		new vscode.Position(0, 0),
		new vscode.Position(editor.document.lineCount, 0)
	);

	const updateCode = new vscode.TextEdit(wholeDocument, code);
	edit.set(editor.document.uri, [updateCode]);
	
	vscode.workspace.applyEdit(edit);
}

function getEditor(): TextEditor {
	const editor = vscode.window.activeTextEditor;
	if (!editor) {
		throw Error('No active editor!');
	}

	return editor;
}

// this method is called when your extension is deactivated
export function deactivate() {}
