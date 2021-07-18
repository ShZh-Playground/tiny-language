package main.java.io.github.tl.resolver;

import main.java.io.github.tl.ast.Stmt;
import main.java.io.github.tl.error.ReturnError;
import main.java.io.github.tl.interpret.Environment;
import main.java.io.github.tl.interpret.Interpreter;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;

    private final Environment closure;

    private final Boolean isInit;

    public Function(Stmt.Function declaration, Environment closure, Boolean isInit) {
        this.closure = (Environment) closure.clone();
        this.declaration = declaration;
        this.isInit = isInit;
    }

    @Override
    public int arity() {
        return this.declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(this.closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ReturnError returnValue) {
            if (this.isInit) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (this.isInit) {
            return this.closure.getAt(0, "this");
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    public Function bind(Instance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new Function(declaration, environment, this.isInit);
    }
}
