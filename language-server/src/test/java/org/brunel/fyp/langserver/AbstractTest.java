package org.brunel.fyp.langserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.expr.AssignExpr;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

public class AbstractTest {
    protected DidChangeConfigurationParams getWorkspaceConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            ArrayNode operatorsArrayNode = mapper.createArrayNode();
            Arrays.stream(AssignExpr.Operator.values()).forEach(operator -> operatorsArrayNode.add(operator.name()));

            ObjectNode operatorsNode = mapper.createObjectNode();
            operatorsNode.put("operators", operatorsArrayNode);

            ObjectNode reduceNode = mapper.createObjectNode();
            reduceNode.put("reduce", operatorsNode);

            ObjectNode refactorNode = mapper.createObjectNode();
            refactorNode.put("refactor", reduceNode);

            ObjectNode javaNode = mapper.createObjectNode();
            javaNode.put("java", refactorNode);

            return new DidChangeConfigurationParams(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(javaNode));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Assertions.fail(e);
        }

        return new DidChangeConfigurationParams(null);
    }
}
