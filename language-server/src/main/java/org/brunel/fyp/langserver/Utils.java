package org.brunel.fyp.langserver;

public class Utils {
    public static String formatFileUri(String fileUri) {
        fileUri = fileUri.replaceAll("\"", "");
        fileUri = fileUri.replaceAll("file:", "");

        return fileUri;
    }
}
