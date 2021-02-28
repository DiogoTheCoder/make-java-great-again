package org.brunel.fyp.langserver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.brunel.fyp.langserver.commands.Commands;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MJGAWorkspaceServiceTest {

    MJGAWorkspaceService mjgaWorkspaceService;

    @BeforeEach
    void setUp() {
        this.mjgaWorkspaceService = MJGALanguageServer.getInstance().getWorkspaceService();
    }

    @Test
    void testExecuteRefactorCommand() {
        String filePath = FileSystems.getDefault().getPath("src/test/resources/patterns/foreach/ForEachToFunctional.java").toAbsolutePath().toString();
        ExecuteCommandParams executeCommandParams = new ExecuteCommandParams(
                Commands.REFACTOR_FILE,
                Collections.singletonList(filePath)
        );

        try {
            // Let's refactor the code
            CompletableFuture<Object> completableFuture = mjgaWorkspaceService.executeCommand(executeCommandParams);
            completableFuture.join();

            // Let's now check if the refactor was done correctly
            CompilationUnit compilationUnit = StaticJavaParser.parse(completableFuture.get().toString());
            String test = "";
        } catch (Throwable e) {
            fail(e);
        }
    }
}