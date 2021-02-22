package org.brunel.fyp.langserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.IOException;
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
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                return e;
            }

            throw new UnsupportedOperationException();
        });
    }

    private CompilationUnit parseFile(String fileUri) throws IOException {
        if (fileUri.isEmpty()) {
            throw new IllegalArgumentException("File URI provided is empty!");
        }

        MJGATextDocumentService mjgaTextDocumentService = MJGALanguageServer.getInstance().getTextDocumentService();
        return mjgaTextDocumentService.parseFile(fileUri);
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
