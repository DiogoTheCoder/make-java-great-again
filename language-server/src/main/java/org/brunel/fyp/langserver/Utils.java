package org.brunel.fyp.langserver;

import com.sun.jna.Platform;

public final class Utils {
    public static final String SOURCE_NAME = "Make Java Great Again";

    public static String formatFileUri(String fileUri) {
        String modifiedFileUri;
        modifiedFileUri = fileUri.replaceAll("\"", "");
        modifiedFileUri = modifiedFileUri.replaceFirst("file://", "");
        modifiedFileUri = modifiedFileUri.replaceFirst("%3A", "");

        if (Platform.isWindows()) {
            modifiedFileUri = modifiedFileUri.replaceFirst("/", "");
            modifiedFileUri = modifiedFileUri.replaceFirst("/", ":\\\\");
            modifiedFileUri = modifiedFileUri.replace("/", "\\");
        }

        return modifiedFileUri;
    }
}
