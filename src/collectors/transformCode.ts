import { CstNode } from 'chevrotain';
import { Block } from 'prettier-plugin-java/src/printers';
import { ForLoopPositionCollector } from './ForLoopPositionCollector';

export function transformCode(cst: CstNode): any {
  let test = "";
  // const forLoopsCollector = new ForLoopPositionCollector();
  // forLoopsCollector.visit(cst);
  // forLoopsCollector.customResult.forEach((values) => {
  //   console.log(values);
  // });
}
