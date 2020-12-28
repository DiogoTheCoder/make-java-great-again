import { parse, readCode, writeCode } from '../utils';
import { transformCode } from '../collectors/transformCode';
import { createPrettierDoc } from 'prettier-plugin-java/src/cst-printer.js';
import { printer } from 'prettier/doc';

export function refactorFile(): void {
  const code = readCode();
  if (typeof code !== 'string') {
    throw Error('Was unable to read code!');
  }

  const cst = parse(code);
  const transformedCode = transformCode(cst);

  const options = {
    printWidth: 80,
    tabWidth: 2,
    useTabs: false,
    trailingComma: "none",
  };

  const doc = createPrettierDoc(cst, options);
  const codeString = printer.printDocToString(doc, options);
  writeCode(codeString.formatted);
}
