package main.java.io.github.tl.interpret;

import java.util.List;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
