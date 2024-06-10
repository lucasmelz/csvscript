## Running

1. Download ANTLR runtime jar to generate parser and lexer. Rename it as 'antlr.jar' to make it easier to execute the commands.

2. Generate Parser and Lexer (if necessary): Run the following command in your project directory where your .g4 grammar file is located:

```
antlr -visitor CsvScript.g4
```

or

```
antlr4 -visitor CsvScript.g4
```

3. Compile all Java classes

```
javac -cp .:antlr.jar *.java

```

4. Execute main function to generate C code

```
java -cp .:antlr.jar Main

```

5. Place the generated code for into the runtime.c main function.

6. Compile and execute the runtime:

```
gcc -o runtime runtime.c && ./runtime
```
