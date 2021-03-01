import org.brunel.fyp.langserver.MJGALanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract public class AbstractTest {
    private final String YYYY_MM_DD = "yyyy-MM-dd";

    public String getLogFilePath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(YYYY_MM_DD);
        String filePath = String.format("src/test/resources/%s.log", simpleDateFormat.format(new Date()));
        return FileSystems.getDefault().getPath(filePath).toAbsolutePath().toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        File logFile = new File(this.getLogFilePath());
        logFile.getParentFile().mkdirs();
        logFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        MJGALanguageServer.getInstance().shutdown();
    }
}
