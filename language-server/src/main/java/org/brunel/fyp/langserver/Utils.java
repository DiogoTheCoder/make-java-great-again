package org.brunel.fyp.langserver;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Level;

public class Utils {
    public static final String SOURCE_NAME = "Make Java Great Again";

    public static String formatFileUri(String fileUri) {
        fileUri = fileUri.replaceFirst("file://", "");
        fileUri = fileUri.replaceFirst("%3A", "");
        fileUri = fileUri.replace("\\", "/");

        MJGALanguageServer.LOGGER.info(fileUri);

        return fileUri;
    }
}
