import { CstNode } from 'chevrotain';
import { ForLoopPositionCollector } from './ForLoopPositionCollector';

export function transformCode(cst: CstNode): any {
  const forLoopsCollector = new ForLoopPositionCollector();
  forLoopsCollector.visit(cst);
  forLoopsCollector.customResult.forEach((values) => {
    console.log(values);
  });
}
