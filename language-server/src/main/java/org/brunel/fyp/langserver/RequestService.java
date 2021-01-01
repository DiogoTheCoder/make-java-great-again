package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@JsonSegment("mjga")
public interface RequestService {
    @JsonRequest
    CompletableFuture<Either<List<CompletionItem>, CompletionList>> refactorCode(CompletionParams var1);
}
