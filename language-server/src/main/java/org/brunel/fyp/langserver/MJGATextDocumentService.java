package org.brunel.fyp.langserver;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.brunel.fyp.langserver.refactorings.ForEachRefactoringPattern;
import org.brunel.fyp.langserver.refactorings.ForLoopRefactoringPattern;
import org.brunel.fyp.langserver.refactorings.RefactorPatternTypes;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MJGATextDocumentService implements TextDocumentService {
    private List<VariableDeclarator> variableDeclarationExprs;

    public MJGATextDocumentService() {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setIgnoreAnnotationsWhenAttributingComments(true);
        parserConfiguration.setDoNotAssignCommentsPrecedingEmptyLines(true);

        StaticJavaParser.setConfiguration(parserConfiguration);
    }

    public CompilationUnit parseFile(String filePath) throws FileNotFoundException {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("Parsing Java code from file: " + filePath);

        CompilationUnit compilationUnit = StaticJavaParser.parse(new FileInputStream(filePath));
        variableDeclarationExprs = compilationUnit.findAll(VariableDeclarator.class);

        return compilationUnit;
    }

    public String refactor(CompilationUnit compilationUnit) {
        for (ForStmt forStmt : compilationUnit.findAll(ForStmt.class)) {
            compilationUnit = new ForLoopRefactoringPattern().refactor(forStmt, compilationUnit);
        }

        for (ForEachStmt forEachStmt : compilationUnit.findAll(ForEachStmt.class)) {
            compilationUnit = new ForEachRefactoringPattern().refactor(forEachStmt, compilationUnit);
        }

        return compilationUnit.toString(new PrettyPrinterConfiguration().setOrderImports(true));
    }

    public void showRefactorableCode(CompilationUnit compilationUnit, String filePath) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ForStmt forStmt : compilationUnit.findAll(ForStmt.class)) {
            Map<RefactorPatternTypes, Boolean> refactorPatternTypes = new ForLoopRefactoringPattern().refactorable(forStmt, compilationUnit);
            if (!refactorPatternTypes.isEmpty()) {
                diagnostics.addAll(this.getDiagnostics(forStmt, refactorPatternTypes));
            }
        }

        for (ForEachStmt forEachStmt : compilationUnit.findAll(ForEachStmt.class)) {
            Map<RefactorPatternTypes, Boolean> refactorPatternTypes = new ForEachRefactoringPattern().refactorable(forEachStmt, compilationUnit);
            if (!refactorPatternTypes.isEmpty()) {
                diagnostics.addAll(this.getDiagnostics(forEachStmt, refactorPatternTypes));
            }
        }

        if (!diagnostics.isEmpty()) {
            MJGALanguageServer.getInstance().getLanguageClient()
                    .publishDiagnostics(new PublishDiagnosticsParams(filePath, diagnostics));
        }
    }

    private List<Diagnostic> getDiagnostics(Node node, Map<RefactorPatternTypes, Boolean> refactorPatternTypes) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (node.getRange().isPresent()) {
            refactorPatternTypes
                    .entrySet()
                    .stream()
                    .filter(Entry::getValue)
                    .map(Entry::getKey)
                    .findFirst()
                    .ifPresent(refactorPattern -> {
                        com.github.javaparser.Range javaRange = node.getRange().get();
                        Range lspRange = new Range(
                                new Position(javaRange.begin.line - 1, javaRange.begin.column - 1),
                                new Position(javaRange.end.line - 1, javaRange.end.column - 1)
                        );

                        diagnostics.add(new Diagnostic(
                                lspRange,
                                "Can refactor this into a " + RefactorPatternTypes.getValue(refactorPattern),
                                DiagnosticSeverity.Information,
                                "Make Java Great Again"
                        ));
                    });
        }

        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(diagnostics.toString());
        return diagnostics;
    }

    public List<VariableDeclarator> getVariableDeclarationExprs() {
        return this.variableDeclarationExprs;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams) {
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem completionItem) {
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams referenceParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams documentSymbolParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams codeActionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams codeLensParams) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens codeLens) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams documentFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams documentRangeFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams renameParams) {
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
        File file = new File(didOpenTextDocumentParams.getTextDocument().getUri().replace("file:", ""));

        try {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(file.getPath());
            CompilationUnit compilationUnit = this.parseFile(file.getPath());
            this.showRefactorableCode(compilationUnit, file.getPath());
        } catch (Exception e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        File file = new File(didChangeTextDocumentParams.getTextDocument().getUri().replace("file:", ""));

        try {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(file.getPath());
            CompilationUnit compilationUnit = this.parseFile(file.getPath());
            this.showRefactorableCode(compilationUnit, file.getPath());
        } catch (Exception e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) { }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) { }
}
