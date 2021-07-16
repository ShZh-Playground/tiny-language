package main.java.io.github.tl;

import main.java.io.github.tl.error.RuntimeError;
import main.java.io.github.tl.interpret.Interpreter;
import main.java.io.github.tl.interpret.Resolver;
import main.java.io.github.tl.parse.Parser;
import main.java.io.github.tl.ast.Stmt;
import main.java.io.github.tl.scan.Scanner;
import main.java.io.github.tl.scan.Token;
import main.java.io.github.tl.scan.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TinyLanguage {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 1) {
            System.out.println("Usage: tl [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException, InterruptedException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while (true) {
            // Distinguish stdout and stderr
            Thread.sleep(100);
            System.out.print(">>> ");
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void run(String source) {
        // Get tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        // Immediately stop compiling
        if (hadError) {
            return;
        }
        // Get expression
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // Immediately stop compiling
        if (hadError) {
            return;
        }
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        // Immediately stop compiling
        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    // Error from scanner
    public static void error(int line, String message) {
        hadError = true;
        System.err.println("Line " + line + ": " + message);
    }

    // Error from parser
    public static void error(Token token, String message) {
        hadError = true;
        if (token.type == TokenType.EOF) {
            System.err.println(token.line + " at end" + message);
        } else {
            System.err.println(token.line + " at '" + token.lexeme + "'" + message);
        }
    }

    // Error from interpreter
    public static void runtimeError(RuntimeError error) {
        hadRuntimeError = true;
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
    }

}
