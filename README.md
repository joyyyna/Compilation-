# Snake Mini Compiler

A small compiler front-end written in **Java** for **SNAKE**, a simple teaching language. It reads a SNAKE program and runs three analysis stages, each available from a Swing window:

| Stage | Class | What it does |
|-------|-------|--------------|
| **Lexical analysis** | `Lexer` | Splits the program into tokens (keywords, identifiers, numbers, strings, symbols, operators, comments) and explains each one. Unknown characters are reported as `inconnu` instead of being skipped |
| **Syntax analysis** | `SyntaxicAnalyzer` | Checks each line against the language rules: instructions end with `#`, program starts with `Snk_Begin` and ends with `Snk_End`, `Begin`/`End` blocks are balanced, `Else` follows an `If`, conditions and `Snk_Print` arguments are well formed, variables are declared before use |
| **Semantic analysis** | `SemanticAnalyzer` | Tracks declared variables and their types, and reports undeclared variables, redeclarations, reserved words used as names, and type mismatches (assignments and conditions) |

University project (compilation course, USTO-MB, M1 AI & Applications).

## The SNAKE language

```
Snk_Begin                  program start
Snk_Int i, j #             declare integers
Snk_Real x1 #              declare a real number
Snk_Strg s #               declare a string
Set i 10 #                 assign a value (literal or variable of the same type)
Get i from j #             Get instruction (both variables must be declared)
If [ i < 20 ]              condition (operators: < > = !=)
Else
Begin ... End              block
Snk_Print "Hello", s #     print strings and/or variables, separated by commas
## comment                 comment line
Snk_End                    program end
```

Every instruction ends with `#`, except `Snk_Begin`, `Snk_End`, `If`, `Else`, `Begin`, `End` and comment lines. Identifiers start with a letter (letters, digits and `_` after). Keywords are reserved and cannot be used as variable names. Blank lines are ignored.

## Project structure

```
Compiler/
└── src/
    ├── MainApp.java            Swing window (load a file, run an analysis)
    ├── Lexer.java
    ├── SyntaxicAnalyzer.java
    ├── SemanticAnalyzer.java
    └── SnakeFileHandler.java   reads the source file (UTF-8, falls back to Windows-1252, strips BOM)
examples/
├── hello.snk                   a valid program
└── errors.snk                  a program with errors
```

All classes are in the default package.

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
## A valid SNAKE program
Snk_Begin
Snk_Int i, j #
Snk_Real x1 #
Snk_Strg s #
Set i 10 #
Set s "Hello, World" #
If [ i < 20 ]
Set j 5 #
Else
Begin
Set x1 15.5 #
End
Snk_Print s, "!" #
Snk_End
```

Syntax analysis:

```
Résultat de l'analyse syntaxique :
Commentaire détecté : ## A valid SNAKE program
Début du programme détecté.
Déclaration détectée : i
Déclaration détectée : j
Déclaration détectée : x1
Déclaration détectée : s
Affectation détectée : Set i 10 #
Affectation détectée : Set s "Hello, World" #
Condition If détectée : If [ i < 20 ]
Affectation détectée : Set j 5 #
Bloc Else détecté.
Début de bloc détecté.
Affectation détectée : Set x1 15.5 #
Fin de bloc détectée.
Instruction d'affichage détectée : Snk_Print s, "!" #
Fin du programme détectée.
Aucune erreur syntaxique.
```

Semantic analysis:

```
Semantic Analysis Result:
Declaration detected: i as int
Declaration detected: j as int
Declaration detected: x1 as real
Declaration detected: s as string
Assignment detected: i = 10
Assignment detected: s = "Hello, World"
Condition detected: i < 20
Assignment detected: j = 5
Assignment detected: x1 = 15.5
Print instruction detected: s
Print instruction detected: "!"
No semantic errors.
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

Syntax analysis (a type mismatch is not a syntax error, so `Set i 2.5 #` passes here):

```
Résultat de l'analyse syntaxique :
Début du programme détecté.
Déclaration détectée : i
Erreur (ligne 3) : '#' manquant à la fin de l'affectation : Set i 10
Erreur (ligne 4) : la variable 'k' n'est pas déclarée.
Affectation détectée : Set i 2.5 #
Fin du programme détectée.
2 erreur(s) syntaxique(s).
```

Semantic analysis:

```
Semantic Analysis Result:
Declaration detected: i as int
Assignment detected: i = 10
Error: Variable 'k' is not declared before assignment. Line: Set k 5 #
Error: Type mismatch in assignment. Expected int but got '2.5'. Line: Set i 2.5 #
2 semantic error(s).
```

## Limitations

This is a learning project. The analyzers work **line by line** and do not build a syntax tree, and the compiler does not generate code. `If` / `Else` / `Begin` / `End` pairing is only counted, not matched structurally. Syntax messages are in French and semantic messages in English.
