package main.java.io.github.tl.resolver;

import main.java.io.github.tl.ast.Expr;
import main.java.io.github.tl.error.RuntimeError;
import main.java.io.github.tl.scan.Token;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    public Klass klass;

    public Map<String, Object> fields = new HashMap<>();

    public Instance(Klass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.getName() + " instance.";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        Function method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method;
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    public void set(Token property, Object value) {
        fields.put(property.lexeme, value);
    }
}
