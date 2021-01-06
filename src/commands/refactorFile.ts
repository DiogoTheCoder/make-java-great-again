import { parse, readCode, writeCode } from '../utils';
import { transformCode } from '../collectors/transformCode';
import { createPrettierDoc } from 'prettier-plugin-java/src/cst-printer.js';
import { printer } from 'prettier/doc';
import { LanguageClient } from 'vscode-languageclient';
import * as vscode from 'vscode';

export async function refactorFile(client: LanguageClient): Promise<void> {
  const code = readCode();
  if (typeof code !== 'string') {
    throw Error('Was unable to read code!');
  }

  let command = await vscode.commands.executeCommand('mjga.langserver.refactorFile', code);
  console.log(command);

  // try {
  //   client.onReady().then(() => {
  //     client.sendRequest("mjga/refactorCode", code)
  //       .then(data => {
  //         console.log(data);
  //       });
  //   }); 
  // } catch (error) {
  //   console.log(error);
  // }

  // const cst = parse(code);
  // const transformedCode = transformCode(cst);

  // const options = {
  //   printWidth: 80,
  //   tabWidth: 2,
  //   useTabs: false,
  //   trailingComma: "none",
  // };

  // const doc = createPrettierDoc(cst, options);
  // const codeString = printer.printDocToString(doc, options);
  // writeCode(codeString.formatted);
}
