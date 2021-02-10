package org.brunel.fyp.langserver.refactorings;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.util.Map;

public interface MJGARefactoringPattern {
    CompilationUnit refactor(Node node, CompilationUnit compilationUnit);
    Map<String, Boolean> refactorable(Node node, CompilationUnit compilationUnit);
}