import org.brunel.fyp.langserver.MJGALanguageServer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class StdioLauncherTest extends AbstractTest {
    @Test
    void startServer() {
        try {
            // TODO: get output streaming working properly
            OutputStream outputStream = new FileOutputStream(this.getLogFilePath());
            LanguageClient languageClient = StdioLauncher.startServer(System.in, outputStream);

            MJGALanguageServer mjgaLanguageServer = MJGALanguageServer.getInstance();

            assertNotNull(mjgaLanguageServer);
            assertNotNull(languageClient);

            outputStream.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
