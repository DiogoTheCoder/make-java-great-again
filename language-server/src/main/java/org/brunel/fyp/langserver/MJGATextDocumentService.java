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
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.brunel.fyp.langserver.MJGALanguageServer.LOGGER;

public class MJGATextDocumentService implements TextDocumentService {
    private List<VariableDeclarator> variableDeclarationExprs;

    public MJGATextDocumentService() {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setIgnoreAnnotationsWhenAttributingComments(true);
        parserConfiguration.setDoNotAssignCommentsPrecedingEmptyLines(true);

        StaticJavaParser.setConfiguration(parserConfiguration);
    }

    public CompilationUnit parseFile(String filePath) throws FileNotFoundException {
        LOGGER.info("Parsing Java code from file: " + filePath);

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

        return diagnostics;
    }

    public List<VariableDeclarator> getVariableDeclarationExprs() {
        return this.variableDeclarationExprs;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams) {
        LOGGER.info(completionParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem completionItem) {
        LOGGER.info(completionItem.toString());
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams textDocumentPositionParams) {
        LOGGER.info(textDocumentPositionParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams textDocumentPositionParams) {
        LOGGER.info(textDocumentPositionParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams textDocumentPositionParams) {
        LOGGER.info(textDocumentPositionParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams referenceParams) {
        LOGGER.info(referenceParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams textDocumentPositionParams) {
        LOGGER.info(textDocumentPositionParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams documentSymbolParams) {
        LOGGER.info(documentSymbolParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams codeActionParams) {
        LOGGER.info(codeActionParams.toString());
        return CompletableFuture.supplyAsync(() -> Collections.singletonList(new Command("Refactor to", "quickfix")));
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams codeLensParams) {
        LOGGER.info(codeLensParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens codeLens) {
        LOGGER.info(codeLens.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams documentFormattingParams) {
        LOGGER.info(documentFormattingParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams documentRangeFormattingParams) {
        LOGGER.info(documentRangeFormattingParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
        LOGGER.info(documentOnTypeFormattingParams.toString());
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams renameParams) {
        LOGGER.info(renameParams.toString());
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
        File file = new File(didOpenTextDocumentParams.getTextDocument().getUri().replace("file:", ""));

        try {
            LOGGER.info(file.getPath());
            CompilationUnit compilationUnit = this.parseFile(file.getPath());
            this.showRefactorableCode(compilationUnit, file.getPath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        File file = new File(didChangeTextDocumentParams.getTextDocument().getUri().replace("file:", ""));

        try {
            LOGGER.info(file.getPath());
            CompilationUnit compilationUnit = this.parseFile(file.getPath());
            this.showRefactorableCode(compilationUnit, file.getPath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) { }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) { }
}
