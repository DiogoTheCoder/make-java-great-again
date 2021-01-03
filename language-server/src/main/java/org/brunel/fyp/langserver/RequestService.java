package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

@JsonSegment("mjga")
public interface RequestService {
    @JsonRequest
    CompletableFuture<String> refactorCode(JavaCodeParams javaCodeParams);
}
