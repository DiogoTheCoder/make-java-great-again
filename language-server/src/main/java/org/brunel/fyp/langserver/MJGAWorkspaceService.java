package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MJGAWorkspaceService implements WorkspaceService {
    private JSONObject configurationSettings;

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            if (params.getCommand().equals("mjga.langserver.refactorFile")) {
                // Get Arguments from VS Code
                File file = new File(params.getArguments().get(0).toString());

                String filePath = file.getPath().replaceAll("\"", "");
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("Parsing Java code from file: " + filePath);

                return MJGALanguageServer.getInstance().getTextDocumentService().parseFile(filePath);
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
        this.configurationSettings = new JSONObject(didChangeConfigurationParams.getSettings().toString())
                .getJSONObject("java");
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }

    public JSONObject getConfigurationSettings() {
        return this.configurationSettings;
    }
}
