package io.github.tl.tools;

import io.github.tl.parse.Expr;
import io.github.tl.scan.Token;
import io.github.tl.scan.TokenType;

public class AstPrinter implements Expr.Visitor<String>  {
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    public String print(Expr expr) {
        return expr == null? "" : expr.accept(this);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group ", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value.toString() == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return parenthesize(expr.name.lexeme);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        String rhs = parenthesize(expr.colon.lexeme, expr.left, expr.right);

        StringBuilder builder = new StringBuilder();
        builder.append("(").append(expr.question.lexeme);
        builder.append(" ");
        builder.append(expr.condition.accept(this));
        builder.append(" ");
        builder.append(rhs);
        builder.append(")");

        return builder.toString();
    }
}
