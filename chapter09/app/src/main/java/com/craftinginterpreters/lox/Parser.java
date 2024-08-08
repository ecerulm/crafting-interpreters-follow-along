package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        // try {
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        // } catch (ParseError e) {
        // }
        return statements;
    }

    /*

      expression     → assignment ;
      assignment     → IDENTIFIER "=" assignment
                     | equality ;”
      equality       → comparison ( ( "!=" | "==" ) comparison )* ;
      comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
      term           → factor ( ( "-" | "+" ) factor )* ;
      factor         → unary ( ( "/" | "*" ) unary )* ;
      unary          → ( "!" | "-" ) unary
                     | primary ;
      primary        → "true" | "false" | "nil"
                     | NUMBER | STRING
                     | "(" expression ")"
                     | IDENTIFIER ;

    */
    private Expr expression() { // one method per grammar rule
        return assignment();
    }

    private Expr assignment() {
        // since we have only LL(1) lookahead
        // we first parse it as an logic_or and if we find a  EQUAL later
        // then it means it was really an assignment

        Expr expr = or();
        // Expr expr = equality();

        // if happend to find a = after it then
        // it means that it was really an assignment
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // recursive

            if (expr instanceof Expr.Variable) {
                // valid target l-value for the assignment
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr; // no equals after the expr means it's just a regular expression
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Stmt declaration() {
        try {
            // declaration: varDeclaration | statement;
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer; // maybe we have one or maybe not
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null; // also optional
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses");

        Stmt body = statement();

        if (increment != null) {
            // the increment goes after the body,
            // create a new body that is a block with
            // previous body + incrementer
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
            // why we need to wrap the increment in an expression
            // increment is already an Stmt.Expression no?
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private void synchronize() {
        // advance until you find a semicolon or the "start of a new statement"
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
                default:
                    // not the start of a new statement so continue skipping tokens
            }
            advance();
        }
    }

    private Stmt printStatement() {
        // the PRINT token was already consumed in statement()
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        // varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
        // the "var" token was consumed already by the rule that calls this rule
        Token name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if (match(EQUAL)) { // this is optional you can declare a variable without initializing it
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'while'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after 'while'.");
      Stmt body = statement();
      return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        // this is NOT called from printStatement()
        Expr value = expression();
        consume(
                SEMICOLON,
                "Expect ';' after expression"); // consume will throw error if there is no match
        return new Stmt.Expression(value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
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

    // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // term           → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // factor         → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // unary          → ( "!" | "-" ) unary
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    // “primary        → "true" | "false" | "nil"
    //                | NUMBER | STRING
    //                | "(" expression ")"
    //                | IDENTIFIER ;”

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError(); // does not throw error , it return an error
    }

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

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
