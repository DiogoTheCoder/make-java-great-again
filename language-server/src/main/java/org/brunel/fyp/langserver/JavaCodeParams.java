package org.brunel.fyp.langserver;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;

public class JavaCodeParams {
  @NonNull
  private String code;

  public JavaCodeParams() {}

  public JavaCodeParams(@NonNull final String code) {
    this.code = code;
  }

  @Pure
  public String getCode() {
    return this.code;
  }
}
