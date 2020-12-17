import {
  BaseJavaCstVisitorWithDefaults,
  BasicForStatementCstNode,
  ForStatementCtx,
} from 'java-parser';

export class ForLoopPositionCollector extends BaseJavaCstVisitorWithDefaults {
  customResult: BasicForStatementCstNode[];
  constructor() {
    super();
    this.customResult = [];
    this.validateVisitor();
  }

  forStatement(ctx: ForStatementCtx) {
    if (
      ctx.basicForStatement &&
      ctx.basicForStatement.length > 0 &&
      ctx.basicForStatement[0]?.children
    ) {
      this.customResult.push(ctx.basicForStatement[0]);
    }
  }
}
