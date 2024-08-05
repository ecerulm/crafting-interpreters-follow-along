
# Grammar


```

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



program        → declaration* EOF ;

declaration    → varDecl
               | statement 
               | block;

block          -> "{" declaration* "}";

statement      → exprStmt
               | printStmt ;”

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
