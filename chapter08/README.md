
# Grammar

```
program        → declaration* EOF ;

declaration    → varDecl
               | statement ;

statement      → exprStmt
               | printStmt ;”

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
