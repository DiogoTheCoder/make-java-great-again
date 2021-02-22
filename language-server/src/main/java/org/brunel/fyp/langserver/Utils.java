package org.brunel.fyp.langserver;

import com.sun.jna.Platform;

public class Utils {
    public static final String SOURCE_NAME = "Make Java Great Again";

    public static String formatFileUri(String fileUri) {
        fileUri = fileUri.replaceAll("\"", "");
        fileUri = fileUri.replaceFirst("file://", "");
        fileUri = fileUri.replaceFirst("%3A", "");

        if (Platform.isWindows()) {
            fileUri = fileUri.replaceFirst("/", "");
            fileUri = fileUri.replaceFirst("/", ":\\\\");
            fileUri = fileUri.replace("/", "\\");
        } else {
            // TODO: check if we need to update the path for UNIX
        }

        return fileUri;
    }
}
