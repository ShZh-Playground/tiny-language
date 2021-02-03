package io.github.tl.parse;

import io.github.tl.TinyLanguage;
import io.github.tl.error.ParseError;
import io.github.tl.scan.Token;
import io.github.tl.scan.TokenType;

import java.util.List;

import static io.github.tl.scan.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private ParseError error(Token token, String message) {
        TinyLanguage.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    //region grammar parser
    public Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return ternary();
    }

    private Expr ternary() {
        Expr expr = comparison();   // condition

        if (match(QUESTION)) {
            Token question = previous();
            Token nextToken = peekNext();
            if (nextToken != null && nextToken.type == COLON) {
                Expr leftExpr = comparison();
                Token colon = advance();
                Expr rightExpr = comparison();
                expr = new Expr.Ternary(expr, question, leftExpr, colon,rightExpr);
            } else {
                throw error(question, " illegal ternary expression.");
            }
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, " Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        // Need one expression(important!!)
        throw error(peek(), " Expect expression.");
    }

    private Token consume(TokenType expected, String message) {
        if (check(expected)) {
            return advance();
        }
        throw error(peek(), message);
    }
    //endregion

    //region iteration part
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        if (!isAtEnd() && tokens.get(current + 1).type != EOF) {
            return tokens.get(current + 1);
        }
        return null;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }
    //endregion
}