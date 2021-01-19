package org.brunel.fyp.langserver;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

public abstract class MJGARefactoringPattern {
    public abstract CompilationUnit refactor(Node node, CompilationUnit compilationUnit);
}