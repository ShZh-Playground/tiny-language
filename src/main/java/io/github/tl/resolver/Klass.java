package main.java.io.github.tl.resolver;

import main.java.io.github.tl.interpret.Interpreter;

import java.util.List;
import java.util.Map;

public class Klass implements Callable {
    private final String name;

    private final Klass superclass;

    private final Map<String, Function> methods;

    public Klass(String name, Klass superclass, Map<String, Function> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        Function init = findMethod("init");
        if (init != null) return init.arity();
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Instance instance = new Instance(this);
        Function init = findMethod("init");
        if (init != null) {
            init.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public Function findMethod(String name) {
        if (this.methods == null) {
            return null;
        }
        Function method = this.methods.get(name);
        if (method == null && this.superclass != null) {
            method = this.superclass.findMethod(name);
        }
        return method;
    }

    public String getName() {
        return name;
    }
}
