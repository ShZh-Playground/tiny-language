package main.java.io.github.tl.scan;

import main.java.io.github.tl.TinyLanguage;
import main.java.io.github.tl.error.ScanError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.io.github.tl.scan.TokenType.*;
import static main.java.io.github.tl.utils.Character.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;      // The first character in lexeme
    private int current = 0;    // Current character index in source file
    private int line = 1;       // Current line in source file

    private static final Map<String, TokenType> keywords;

    // Key word definition
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    private static void error(int line, String message) {
        TinyLanguage.error(line, message);
        throw new ScanError();
    }

    //region token scanner part
    public List<Token> scanTokens() {
        try {
            while (!isAtEnd()) {
                start = current;
                scanToken();
            }
        } catch (ScanError e) {
            return null;
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character token
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON);  break;
            case '/':
                if (match('/')) {
                    // Line comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Block comment
                    blockComment();
                } else {
                    // Normal slash
                    addToken(SLASH);
                }
                break;
            // One or two character token
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            // Ignore whitespace.
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            // Literals
            case '"':
            case '\'':
                string(c); break;
            default:
                // We cannot judge number in case statement
                // The only way to scan number is in default branch
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error(line, "Unexpected character.");
                }
                break;
        }
    }
    //endregion

    //region character dealing part
    private char advance() {
        ++current;
        return source.charAt(current - 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char excepted) {
        if (!isAtEnd() && source.charAt(current) == excepted) {
            ++current;
            return true;
        }
        return false;
    }
    //endregion

    //region difficult handling tokens
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void string(char c) {
        while (peek() != c && !isAtEnd()) {
            if (peek() == '\n') {
                ++line;
            }
            advance();
        }
        // The " must be paired
        if (isAtEnd()) {
            error(line, "Unterminated string.");
            return;
        }
        // The closing " or '.
        advance();
        // Trim the surrounding quotes
        // And add to tokens array
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void blockComment() {
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') {
                ++line;
            }
            advance();
        }
        if (isAtEnd()) {
            error(line, "Unterminated block comment.");
            return;
        }
        // The closing * and /
        advance();
        advance();
    }
    //endregion

    //region add token part
    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }
    //endregion
}
