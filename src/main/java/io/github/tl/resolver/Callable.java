package main.java.io.github.tl.resolver;

import main.java.io.github.tl.interpret.Interpreter;

import java.util.List;

public interface Callable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
