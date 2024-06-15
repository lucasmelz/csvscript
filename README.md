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

4. Transpile csvs program to C, compile and execute the program with:

```
java -cp .:antlr.jar  Main <YOUR_CSVS_PROGRAM>.csvs
```
