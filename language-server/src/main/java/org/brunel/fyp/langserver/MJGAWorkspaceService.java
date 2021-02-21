package org.brunel.fyp.langserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.brunel.fyp.langserver.MJGALanguageServer.LOGGER;

public class MJGAWorkspaceService implements WorkspaceService {
    private JsonNode configurationSettings;

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (params.getCommand().equals("mjga.langserver.refactorFile")) {
                    String fileUri = params.getArguments().get(0).toString();
                    CompilationUnit compilationUnit = this.parseFile(fileUri);
                    return MJGALanguageServer.getInstance().getTextDocumentService().refactor(compilationUnit);
                } else if (params.getCommand().equals("mjga.langserver.refactorSnippet")) {
                    List<Object> arguments = params.getArguments();
                    String textDocumentIdentifierString = arguments.get(0).toString();
                    String diagnosticString = arguments.get(1).toString();

                    if (textDocumentIdentifierString.isEmpty() || diagnosticString.isEmpty()) {
                        throw new RuntimeException("Invalid arguments for refactorSnippet");
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    TextDocumentIdentifier textDocumentIdentifier = objectMapper.readValue(textDocumentIdentifierString, TextDocumentIdentifier.class);
                    Diagnostic diagnostic = objectMapper.readValue(diagnosticString, Diagnostic.class);

                    CompilationUnit compilationUnit = this.parseFile(textDocumentIdentifier.getUri());
                    String code = MJGALanguageServer.getInstance().getTextDocumentService().refactorSnippet(compilationUnit, diagnostic.getRange());
                    LOGGER.info(code);
                    return code;
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                return e;
            }

            throw new UnsupportedOperationException();
        });
    }

    private CompilationUnit parseFile(String fileUri) throws FileNotFoundException {
        if (fileUri.isEmpty()) {
            throw new RuntimeException("File URI provided is empty!");
        }

        File file = new File(fileUri);

        String filePath = file.getPath().replaceAll("\"", "");
        filePath = filePath.replaceAll("file:", "");
        LOGGER.info("Parsing Java code from file: " + filePath);

        MJGATextDocumentService mjgaTextDocumentService = MJGALanguageServer.getInstance().getTextDocumentService();
        return mjgaTextDocumentService.parseFile(filePath);
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams workspaceSymbolParams) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.configurationSettings = mapper.readTree(didChangeConfigurationParams.getSettings().toString()).get("java");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }

    public JsonNode getConfigurationSettings() {
        return this.configurationSettings;
    }
}
