import { parse, readCode } from '../utils';
import { transformCode } from '../collectors/transformCode';

export function refactorFile(): void {
  const code = readCode();
  if (typeof code !== 'string') {
    throw Error('Was unable to read code!');
  }

  const cst = parse(code);
  const transformedCode = transformCode(cst);
  //writeCode(transformedCode);
}
