package main.java.io.github.tl.interpret;

import main.java.io.github.tl.error.RuntimeError;
import main.java.io.github.tl.scan.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment implements Cloneable {
    public Environment enclosing;
    private Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    @Override
    public Object clone() {
        Environment dup = null;
        try {
            dup = (Environment) super.clone();
            dup.values = new HashMap<>(this.values);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return dup;
    }
}
