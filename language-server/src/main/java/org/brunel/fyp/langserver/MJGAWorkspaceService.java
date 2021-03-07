package org.brunel.fyp.langserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.serialization.JavaParserJsonSerializer;
import org.brunel.fyp.langserver.commands.Commands;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.brunel.fyp.langserver.MJGALanguageServer.LOGGER;

public class MJGAWorkspaceService implements WorkspaceService {
    private JsonNode configurationSettings;

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileUri = params.getArguments().get(0).toString();
                CompilationUnit compilationUnit = this.parseFile(fileUri);
                String command = params.getCommand();
                if (command.equals(Commands.REFACTOR_FILE)) {
                    return MJGALanguageServer.getInstance().getTextDocumentService().refactor(compilationUnit);
                } else if (command.equals(Commands.GENERATE_DOT_AST)) {
                    DotPrinter dotPrinter = new DotPrinter(true);
                    return dotPrinter.output(compilationUnit);
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                return e;
            }

            throw new UnsupportedOperationException();
        });
    }

    private String serialise(Node node) {
        JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(new HashMap<>());
        JavaParserJsonSerializer serializer = new JavaParserJsonSerializer();
        StringWriter jsonWriter = new StringWriter();
        try (JsonGenerator generator = generatorFactory.createGenerator(jsonWriter)) {
            serializer.serialize(node, generator);
        }

        return jsonWriter.toString();
    }

    private CompilationUnit parseFile(String fileUri) throws IOException {
        if (fileUri.isEmpty()) {
            throw new IllegalArgumentException("File URI provided is empty!");
        }

        MJGATextDocumentService mjgaTextDocumentService = MJGALanguageServer.getInstance().getTextDocumentService();
        return mjgaTextDocumentService.parseFile(fileUri);
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams workspaceSymbolParams) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.configurationSettings = mapper.readTree(didChangeConfigurationParams.getSettings().toString()).get("java");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {
        LOGGER.info(didChangeWatchedFilesParams.toString());
    }

    public JsonNode getConfigurationSettings() {
        return this.configurationSettings;
    }
}
