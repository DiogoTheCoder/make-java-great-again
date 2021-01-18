package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class MJGAWorkspaceService implements WorkspaceService {
    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            if (params.getCommand().equals("mjga.langserver.refactorFile")) {
                File file = new File(params.getArguments().get(0).toString());
                String filePath = file.getPath().replaceAll("\"", "");
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("Parsing Java code from file: " + filePath);
                CompilationUnit compilationUnit;
                try {
                    compilationUnit = StaticJavaParser.parse(new FileInputStream(filePath));
                    List<VariableDeclarator> variableDeclarationExprs = compilationUnit.findAll(VariableDeclarator.class);
                    compilationUnit.findAll(ForEachStmt.class)
                        .stream()
                        .forEach(forEachStmt -> {
                            ExpressionStmt eStmt = new ExpressionStmt();
        
                            NameExpr iterableExpression = forEachStmt.getIterable().asNameExpr();
        
                            VariableDeclarator arrayDeclarationExpr = variableDeclarationExprs
                                .stream()
                                .filter(variableDeclarationExpr -> variableDeclarationExpr.getName().equals(iterableExpression.getName()))
                                .findFirst()
                                .get();
        
                            // Arrays.stream(name) or name.stream()
                            MethodCallExpr methodCallExprArrays = new MethodCallExpr();
                            // stream
                            SimpleName simpleNameStream = new SimpleName("stream");
        
                            // Is this an array, e.g. String[]?
                            Type arrayDeclarationType = arrayDeclarationExpr.getType();
                            NameExpr nameExprArrays;
                            if (arrayDeclarationType.isArrayType()) {
                                compilationUnit.addImport(new ImportDeclaration("java.util.Arrays", false, false));
                                nameExprArrays = new NameExpr(new SimpleName("Arrays"));
        
                                // Name of the array to loop, argument for Arrays.stream()
                                methodCallExprArrays.setArguments(new NodeList<Expression>(iterableExpression));
                            } else {
                                nameExprArrays = iterableExpression;
                            }
        
                            nameExprArrays.setParentNode(methodCallExprArrays);
                            methodCallExprArrays.setScope(nameExprArrays);
                            methodCallExprArrays.setName(simpleNameStream);
        
                            // Arrays.stream(name), forEach, "string -> {
                            MethodCallExpr methodCallExpr = new MethodCallExpr();
        
                            // forEach
                            methodCallExpr.setName(new SimpleName("forEach"));
        
                            // string
                            Parameter parameterString = new Parameter(new UnknownType(),forEachStmt.getVariableDeclarator().getName());
                            // "string -> {
                            LambdaExpr lambdaExpr = new LambdaExpr(parameterString, (BlockStmt) forEachStmt.getBody());
        
                            // Arrays.streams(name), "string ->
                            methodCallExpr.setArguments(new NodeList<Expression>(lambdaExpr));
        
                            methodCallExpr.setScope(methodCallExprArrays);
        
                            eStmt.setExpression(methodCallExpr);
        
                            forEachStmt.replace(eStmt);
                        });

                    return compilationUnit.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }

            throw new UnsupportedOperationException();
        });
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams workspaceSymbolParams) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }
}