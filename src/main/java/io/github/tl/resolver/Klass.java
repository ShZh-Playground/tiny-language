package main.java.io.github.tl.resolver;

import main.java.io.github.tl.interpret.Interpreter;

import java.util.List;

public class Klass implements Callable {
    public String name;

    public Klass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new Instance(this);
    }
}
