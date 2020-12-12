import { BaseJavaCstVisitorWithDefaults, MethodBodyCstNode, MethodBodyCtx } from "java-parser";

export class MethodBodyPositionCollector extends BaseJavaCstVisitorWithDefaults {
  customResult: MethodBodyCstNode[];
  constructor() {
    super();
    this.customResult = [];
    this.validateVisitor();
  }

  methodBody(ctx: MethodBodyCtx) {
    if (ctx.block && ctx.block.length > 0 && ctx.block[0]?.children) {
      // this.customResult.push(ctx.block.);
    }
  }
}
