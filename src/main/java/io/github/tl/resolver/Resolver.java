package main.java.io.github.tl.resolver;

import main.java.io.github.tl.ast.Expr;
import main.java.io.github.tl.ast.Stmt;
import main.java.io.github.tl.interpret.Interpreter;
import main.java.io.github.tl.scan.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static main.java.io.github.tl.TinyLanguage.error;

public class Resolver implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD
    }

    private Interpreter interpreter;

    private Stack<Map<String, Boolean>> scopes = new Stack<>();

    private FunctionType currentFunctionType = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument: expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);

        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.condition);
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(method, declaration);
        }

        endScope();

        define(stmt.name);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (this.currentFunctionType == FunctionType.NONE) {
            error(stmt.keyword, "Can't return from top-level code.");
        }

        resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    //region resolve functions
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            error(name, "Already variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType functionType) {
        FunctionType enclosingFunction = this.currentFunctionType;
        this.currentFunctionType = functionType;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        this.currentFunctionType = enclosingFunction;
    }
    //endregion
}
