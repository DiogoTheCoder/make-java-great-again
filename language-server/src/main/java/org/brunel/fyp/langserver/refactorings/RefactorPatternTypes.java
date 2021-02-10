package org.brunel.fyp.langserver.refactorings;

public enum RefactorPatternTypes {
    FOR_EACH,
    MAP,
    REDUCE;

    public static String getValue(RefactorPatternTypes refactorPatternTypes) {
        switch (refactorPatternTypes) {
            case FOR_EACH:
                return "forEach";
            case MAP:
                return "map";
            case REDUCE:
                return "reduce";
        }

        return "";
    }
}
