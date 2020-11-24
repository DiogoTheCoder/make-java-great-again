import { parse } from "java-parser";
import { ForLoopPositionCollector } from "./ForLoopPositionCollector";

export function transformCode(code: string): any {
  const cst: CstNode = parse(code);

  const forLoopsCollector = new ForLoopPositionCollector();
  forLoopsCollector.visit(cst);
  forLoopsCollector.customResult.forEach(values => {
    console.log(values);
  }); 
}