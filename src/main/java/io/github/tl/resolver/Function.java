package main.java.io.github.tl.resolver;

import main.java.io.github.tl.ast.Stmt;
import main.java.io.github.tl.error.ReturnError;
import main.java.io.github.tl.interpret.Environment;
import main.java.io.github.tl.interpret.Interpreter;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;

    private final Environment closure;

    public Function(Stmt.Function declaration, Environment closure) {
        this.closure = (Environment) closure.clone();
        this.declaration = declaration;
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
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
