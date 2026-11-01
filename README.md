# Snake Mini Compiler

A small compiler front-end written in **Java** for **SNAKE**, a simple teaching language. It reads a SNAKE program and runs three analysis stages, each available from a Swing window:

| Stage | Class | What it does |
|-------|-------|--------------|
| **Lexical analysis** | `Lexer` | Splits the program into tokens (keywords, identifiers, numbers, strings, symbols, operators) and explains each one |
| **Syntax analysis** | `SyntaxicAnalyzer` | Checks each line against the language rules (instructions end with `#`, program starts with `Snk_Begin`, ends with `Snk_End`, variables are declared before use) |
| **Semantic analysis** | `SemanticAnalyzer` | Tracks declared variables and their types, and reports undeclared variables, redeclarations and type mismatches |

University project (compilation course, USTO-MB, M1 AI & Applications).

## The SNAKE language

```
Snk_Begin                  program start
Snk_Int i, j #             declare integers
Snk_Real x1 #              declare a real number
Snk_Strg s #               declare a string
Set i 10 #                 assign a value
Get i from j #             Get instruction (both variables must be declared)
If [ i < 20 ]              condition (operators: < > = !=)
Else
Begin ... End              block
Snk_Print "Hello" #        print a string or a variable
## comment                 comment line
Snk_End                    program end
```

Every instruction ends with `#`.

## Project structure

```
Compiler/
└── src/
    ├── MainApp.java            Swing window (load a file, run an analysis)
    ├── Lexer.java
    ├── SyntaxicAnalyzer.java
    ├── SemanticAnalyzer.java
    └── SnakeFileHandler.java   reads the source file
examples/
├── hello.snk                   a valid program
└── errors.snk                  a program with errors
```

## Build and run

You need a JDK (Java 11 or newer).

```bash
cd Compiler
javac -encoding UTF-8 -d bin src/*.java
java -cp bin MainApp
```

In the window: **Load File** → pick a file from `examples/` → click **Lexical Analysis**, **Syntax Analysis** or **Semantic Analysis**.

## Example

`examples/hello.snk`:

```
Snk_Begin
Snk_Int i, j #
Snk_Real x1 #
Set i 10 #
If [ i < 20 ]
Set j 5 #
Else
Begin
Set x1 15.5 #
End
Snk_Print "Hello" #
Snk_End
```

Semantic analysis:

```
Semantic Analysis Result:
Declaration detected: i as int
Declaration detected: j as int
Declaration detected: x1 as real
Assignment detected: i = 10
Assignment detected: j = 5
Assignment detected: x1 = 15.5
Print instruction detected: "Hello"
```

`examples/errors.snk` (a missing `#`, an undeclared variable, and a real assigned to an integer):

```
Snk_Begin
Snk_Int i #
Set i 10
Set k 5 #
Set i 2.5 #
Snk_End
```

Syntax analysis:

```
Résultat de l'analyse syntaxique :
Début du programme détecté.
Déclaration détectée : i
Erreur de syntaxe dans l'affectation : Set i 10
Erreur : La variable 'k' n'est pas déclarée. Ligne : Set k 5 #
Affectation détectée : Set i 2.5 #
Fin du programme détectée.
```

Semantic analysis:

```
Semantic Analysis Result:
Declaration detected: i as int
Assignment detected: i = 10
Error: Variable 'k' is not declared before assignment. Line: Set k 5 #
Error: Type mismatch in assignment. Expected int but got '2.5'. Line: Set i 2.5 #
```

## Limitations

This is a learning project. The analyzers work **line by line** and do not build a syntax tree, and the compiler does not generate code. Some messages are in French and some in English.
