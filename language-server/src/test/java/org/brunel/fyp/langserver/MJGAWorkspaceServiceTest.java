package org.brunel.fyp.langserver;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.brunel.fyp.langserver.commands.Commands;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

            // Should be of size two, beforeRefactor() and afterRefactor()
            List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);
            assertEquals(2, methodDeclarations.size());

            MethodDeclaration beforeRefactorMethod = methodDeclarations.get(0);
            MethodDeclaration afterRefactorMethod = methodDeclarations.get(1);

            // Now let's check both before and after refactor methods have the same code!
            assertEquals(afterRefactorMethod.getBody().get().toString(), beforeRefactorMethod.getBody().get().toString());
        } catch (Throwable e) {
            fail(e);
        }
    }
}