import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.brunel.fyp.langserver.MJGALanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class StdioLauncher {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.ALL);

        // Start the language server
        startServer(System.in, System.out);
    }

    /**
     * Start up the Make Java Great Again Language Server
     *
     * @param in
     * @param out
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    public static LanguageClient startServer(InputStream in, OutputStream out) throws ExecutionException, InterruptedException {
        // Initialize the MJGALanguageServer
        MJGALanguageServer languageServer = MJGALanguageServer.getInstance();

        // Create JSON RPC launcher for MJGALanguageServer instance.
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(languageServer, in, out);

        // Get the client that request to launch the LS.
        LanguageClient client = launcher.getRemoteProxy();

        // Set the client to language server
        languageServer.connect(client);

        // Start the listener for JsonRPC
        launcher.startListening();

        return client;
    }
}
