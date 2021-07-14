package main.java.io.github.tl.error;

import main.java.io.github.tl.scan.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
