package main.java.io.github.tl.resolver;

public class Instance {
    public Klass klass;

    public Instance(Klass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance.";
    }
}
