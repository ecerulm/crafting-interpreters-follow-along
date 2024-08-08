
# Grammar


```

expression     → assignment ;  // lower precedence
assignment     → IDENTIFIER "=" assignment
               | logic_or ;”
logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )*
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ; //highest precedence



program        → declaration* EOF ;

declaration    → varDecl
               | statement 
               | block;

block          -> "{" declaration* "}";

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | whileStmt
               | block ;

forStmt        → "for" "(" 
                 ( varDecl | exprStmt | ";") // varDecl and exprStmt already have a ";" at the end
                 expression? ";"
                 expression? ")" statement;
               ;
whileStmt      → "while" "(" expression ")" statement ;
ifStmt         → "if" "(" expression ")" statement
               ( "else" statement )? ;

Excerpt From
Crafting Interpreters
Robert Nystrom
This material may be protected by copyright.

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;



```
# Generate the ast classes 


```
pyenv virtualenv 3.12 craftinginterpreters
source $(pyenv prefix craftinginterpreters)/bin/activate
```





# build

```
./gradlew build
$(brew --prefix openjdk@21)/bin/java -classpath ./app/bin/main com.craftinginterpreters.lox.Lox
```


# Run REPL 


```
$(brew --prefix openjdk@21)/bin/java -classpath ./app/bin/main com.craftinginterpreters.lox.Lox
```

# Running AstPrinter

```
(brew --prefix openjdk@21)/bin/java -cp ./app/build/classes/java/main com.craftinginterpreters.lox.AstPrinter
```

will output

```
(* (- 123) (group 45.67))
```
