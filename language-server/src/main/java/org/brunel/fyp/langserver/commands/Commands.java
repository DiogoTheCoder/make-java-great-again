package org.brunel.fyp.langserver.commands;

public class Commands {
    public static final String REFACTOR_FILE = "mjga.langserver.refactorFile";
    public static final String REFACTOR_SNIPPET = "mjga.langserver.refactorSnippet";
    public static final String GENERATE_JSON_AST = "mjga.langserver.generateJsonAST";

    public static final String[] ALL_COMMANDS = { REFACTOR_FILE, REFACTOR_SNIPPET, GENERATE_JSON_AST };
}
