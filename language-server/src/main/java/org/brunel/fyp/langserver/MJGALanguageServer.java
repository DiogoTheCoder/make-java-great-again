package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MJGALanguageServer implements LanguageServer, LanguageClientAware {
    public static Logger LOGGER;
    private static MJGALanguageServer INSTANCE;

    private LanguageClient languageClient;

    private final MJGATextDocumentService textDocumentService;
    private final MJGAWorkspaceService workspaceService;

    private int errorCode = 1;

    private MJGALanguageServer() {
        LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        LOGGER.info("Instantiating " + this.getClass().getSimpleName() + "...");
        this.textDocumentService = new MJGATextDocumentService();
        this.workspaceService = new MJGAWorkspaceService();
    }

    public static MJGALanguageServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MJGALanguageServer();
        }

        return INSTANCE;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        // Initialize the InitializeResult for this LS.
        final InitializeResult initializeResult = new InitializeResult(new ServerCapabilities());
        
        // Set the capabilities of the LS to inform the client.
        ServerCapabilities capabilities = initializeResult.getCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(true);

        List<String> commands = Arrays.asList("mjga.langserver.refactorFile", "mjga.langserver.refactorSnippet");
        ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commands);
        capabilities.setExecuteCommandProvider(executeCommandOptions);

        initializeResult.setCapabilities(capabilities);
        return CompletableFuture.supplyAsync(()->initializeResult);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        // If shutdown request comes from client, set the error code to 0.
        errorCode = 0;
        return null;
    }

    @Override
    public void exit() {
        // Kill the LS on exit request from client.
        System.exit(errorCode);
    }

    @Override
    public MJGATextDocumentService getTextDocumentService() {
        // Return the endpoint for language features.
        return this.textDocumentService ;
    }

    @Override
    public MJGAWorkspaceService getWorkspaceService() {
        // Return the endpoint for workspace functionality.
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient languageClient) {
        // Get the client which started this LS.
        this.languageClient = languageClient;
    }

    public LanguageClient getLanguageClient() {
        return this.languageClient;
    }
}
