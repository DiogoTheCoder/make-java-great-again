package org.brunel.fyp.langserver;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.brunel.fyp.langserver.refactorings.ForEachRefactoringPattern;
import org.brunel.fyp.langserver.refactorings.ForLoopRefactoringPattern;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
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
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
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

    public JSONObject getRefactorableCode(CompilationUnit compilationUnit) {
        for (ForStmt forStmt : compilationUnit.findAll(ForStmt.class)) {
//            compilationUnit = new ForLoopRefactoringPattern().refactorable(forStmt, compilationUnit);
        }

        for (ForEachStmt forEachStmt : compilationUnit.findAll(ForEachStmt.class)) {
//            compilationUnit = new ForEachRefactoringPattern().refactor(forEachStmt, compilationUnit);
        }

        return null;
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
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) { }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        File file = new File(didChangeTextDocumentParams.getTextDocument().getUri());

        String filePath = file.getPath().replaceAll("\"", "");
        try {
            CompilationUnit compilationUnit = this.parseFile(filePath);
            this.getRefactorableCode(compilationUnit);
        } catch (Exception e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) { }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) { }
}
