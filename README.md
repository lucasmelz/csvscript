# CsvScript

## About the project

CsvScript is a domain specific language created with the purpose of manipulating .csv files representing matrices of integers. This project is a transpiler for a custom language called CsvScript. It converts CsvScript code into C code, compiles it, and executes the resulting program. The language is designed to work with matrices, vectors, and scalars, providing an easy way to perform linear algebra operations with the matrices stored on CSV files.

## Types

CsvScript has three diferent data types, matrices of integers, vectors of integers and scalars (also integers).

## Syntax

CsvScript supports the following syntax elements:

1. Persistent Matrix declaration:

```
persistent matrix_name
```

Means that you have a `matrix_name.csv` file stored in the `csv_samples` folder, that will be loaded into the program and modified once the program finishes running. All operations done to the `matrix_name` variable will be saved to the corresponding csv file once the program finishes its execution.

2. Assignments:

```
<variable> = <expression>
```

The input data is solely csv files that are loaded into the program as matrices. From a matrix, you can extract a row like this:

```
row = matrix[0]
```

Or a scalar, like this:

```
element = matrix[0][1]
```

You can also select an element from a vector:

```
element = row[0]
```

And have a variable to store a scalar:

```
scalar = 42
```

3. Expressions:

- Transpose a matrix

```
transpose matrix
```

- Multiplication

  You can multiply matrices, a matrix by a scalar, a vector by a scalar and perform scalar multiplication.

```
result = var1 * var2
```

- Division

  For now, you can only perform integer division between scalars:

```
result = scalar1 / scalar 2
```

- Addition

  You can add matrices, add a scalar to a matrix, add a scalar to a vector and add scalars.

  ```
  result = var1 + var2
  ```

- Subtraction

  You can subtract a scalar from a matrix, a scalar from a vector and perform subtraction between scalars.

  ```
  result = var1 - var2
  ```

You can find examples of CsvScript programs in the examples folder of this project.

## How to run a CsvScript program.

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
java -cp .:antlr.jar  Main <YOUR_CSVS_PROGRAM>.csvs <PATH_AND_FILE_OUTPUT_NAME>
```

This will create in the current folder a .c and a .o file with the <FILE_OUTPUT_NAME> that you choose.
