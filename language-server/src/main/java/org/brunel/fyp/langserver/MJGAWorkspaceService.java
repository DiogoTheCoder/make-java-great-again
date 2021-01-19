package org.brunel.fyp.langserver;

import org.brunel.fyp.langserver.refactorings.ForEachRefactoringPattern;
import org.brunel.fyp.langserver.refactorings.ForLoopRefactoringPattern;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

public class MJGAWorkspaceService implements WorkspaceService {
    public static CompilationUnit compilationUnit;
    public static List<VariableDeclarator> variableDeclarationExprs;

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            if (params.getCommand().equals("mjga.langserver.refactorFile")) {
                File file = new File(params.getArguments().get(0).toString());
                String filePath = file.getPath().replaceAll("\"", "");
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("Parsing Java code from file: " + filePath);
                try {
                    ParserConfiguration parserConfig = new ParserConfiguration();
                    parserConfig.setIgnoreAnnotationsWhenAttributingComments(true);
                    StaticJavaParser.setConfiguration(parserConfig);
                    compilationUnit = StaticJavaParser.parse(new FileInputStream(filePath));
                    variableDeclarationExprs = compilationUnit.findAll(VariableDeclarator.class);
                    compilationUnit.findAll(ForStmt.class)
                        .stream()
                        .forEach(forStmt -> {
                            compilationUnit = new ForLoopRefactoringPattern().refactor(forStmt, compilationUnit);
                        });

                    compilationUnit.findAll(ForEachStmt.class)
                        .stream()
                        .forEach(forEachStmt -> {
                            compilationUnit = new ForEachRefactoringPattern().refactor(forEachStmt, compilationUnit);
                        });

                    return compilationUnit.toString(new PrettyPrinterConfiguration().setOrderImports(true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }

            throw new UnsupportedOperationException();
        });
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams workspaceSymbolParams) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }
}
