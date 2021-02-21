package org.brunel.fyp.langserver;

public class Utils {
    public static final String SOURCE_NAME = "Make Java Great Again";

    public static String formatFileUri(String fileUri) {
        fileUri = fileUri.replaceAll("\"", "");
        fileUri = fileUri.replaceFirst("//", "");
        fileUri = fileUri.replaceFirst("file:", "");

        return fileUri;
    }
}
