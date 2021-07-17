package main.java.io.github.tl.resolver;

import main.java.io.github.tl.interpret.Interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Klass implements Callable {
    private final String name;

    private Map<String, Function> methods = new HashMap<>();

    public Klass(String name, Map<String, Function> methods) {
        this.name = name;
        this.methods = methods;
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

    public Function findMethod(String name) {
        return this.methods.get(name);
    }

    public String getName() {
        return name;
    }
}
