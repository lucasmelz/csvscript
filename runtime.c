#include <stdio.h>
#include <stdlib.h>
#include "runtime.h"

// Function to load a matrix from a CSV file
Matrix *load_matrix_from_csv(const char *filename)
{
    int rows, cols;
    count_rows_and_cols(filename, &rows, &cols);
    FILE *file = fopen(filename, "r");
    if (!file)
        return NULL;

    Matrix *matrix = (Matrix *)malloc(sizeof(Matrix));
    matrix->rows = rows;
    matrix->cols = cols;
    matrix->data = (int **)malloc(rows * sizeof(int *));
    for (int i = 0; i < rows; i++)
    {
        matrix->data[i] = (int *)malloc(cols * sizeof(int));
        for (int j = 0; j < cols; j++)
        {
            if (fscanf(file, "%d,", &matrix->data[i][j]) != 1)
            {
                fclose(file);
                free_matrix(matrix);
                return NULL;
            }
        }
    }

    fclose(file);
    return matrix;
}

// Function to save a matrix to a CSV file
void save_matrix_to_csv(const char *filename, Matrix *matrix)
{
    FILE *file = fopen(filename, "w");
    if (!file)
        return;

    for (int i = 0; i < matrix->rows; i++)
    {
        for (int j = 0; j < matrix->cols; j++)
        {
            fprintf(file, "%d", matrix->data[i][j]);
            if (j < matrix->cols - 1)
                fprintf(file, ",");
        }
        fprintf(file, "\n");
    }

    fclose(file);
}

// Function to transpose a matrix
Matrix *transpose_matrix(Matrix *matrix)
{
    Matrix *transposed = (Matrix *)malloc(sizeof(Matrix));
    transposed->rows = matrix->cols;
    transposed->cols = matrix->rows;
    transposed->data = (int **)malloc(transposed->rows * sizeof(int *));
    for (int i = 0; i < transposed->rows; i++)
    {
        transposed->data[i] = (int *)malloc(transposed->cols * sizeof(int));
        for (int j = 0; j < transposed->cols; j++)
        {
            transposed->data[i][j] = matrix->data[j][i];
        }
    }
    return transposed;
}

// Function to multiply two matrices
Matrix *multiply_matrices(Matrix *matrix1, Matrix *matrix2)
{
    if (matrix1->cols != matrix2->rows)
        return NULL;

    Matrix *result = (Matrix *)malloc(sizeof(Matrix));
    result->rows = matrix1->rows;
    result->cols = matrix2->cols;
    result->data = (int **)malloc(result->rows * sizeof(int *));
    for (int i = 0; i < result->rows; i++)
    {
        result->data[i] = (int *)malloc(result->cols * sizeof(int));
        for (int j = 0; j < result->cols; j++)
        {
            result->data[i][j] = 0;
            for (int k = 0; k < matrix1->cols; k++)
            {
                result->data[i][j] += matrix1->data[i][k] * matrix2->data[k][j];
            }
        }
    }
    return result;
}

// Function to add two matrices
Matrix *add_matrices(Matrix *m1, Matrix *m2)
{
    if (m1->rows != m2->rows || m1->cols != m2->cols)
    {
        fprintf(stderr, "Matrices should have same number of rows and columns to be added.\n");
        return NULL;
    }

    Matrix *result = (Matrix *)malloc(sizeof(Matrix));
    result->rows = m1->rows;
    result->cols = m1->cols;
    result->data = (int **)malloc(result->rows * sizeof(int *));

    for (int i = 0; i < m1->rows; i++)
    {
        result->data[i] = (int *)malloc(result->cols * sizeof(int));
        for (int j = 0; j < m1->cols; j++)
        {
            result->data[i][j] = m1->data[i][j] + m2->data[i][j];
        }
    }
    return result;
}

// Function to add a scalar to all elements of a matrix
Matrix *add_scalar_to_matrix(Matrix *matrix, int scalar)
{
    Matrix *result = (Matrix *)malloc(sizeof(Matrix));
    result->rows = matrix->rows;
    result->cols = matrix->cols;
    result->data = (int **)malloc(result->rows * sizeof(int *));

    for (int i = 0; i < result->rows; i++)
    {
        result->data[i] = (int *)malloc(result->cols * sizeof(int));
        for (int j = 0; j < result->cols; j++)
        {
            result->data[i][j] = matrix->data[i][j] + scalar;
        }
    }

    return result;
}

// Function to multiply a matrix by a scalar and return the result as a new matrix
Matrix *multiply_matrix_by_scalar(Matrix *matrix, int scalar)
{
    Matrix *result = (Matrix *)malloc(sizeof(Matrix));
    result->rows = matrix->rows;
    result->cols = matrix->cols;
    result->data = (int **)malloc(result->rows * sizeof(int *));

    for (int i = 0; i < result->rows; i++)
    {
        result->data[i] = (int *)malloc(result->cols * sizeof(int));
        for (int j = 0; j < result->cols; j++)
        {
            result->data[i][j] = matrix->data[i][j] * scalar;
        }
    }

    return result;
}

// Function to extract a row from a matrix and return it as a vector
Vector *extract_row(Matrix *matrix, int row_index)
{
    if (row_index >= matrix->rows)
    {
        fprintf(stderr, "Row index out of bounds.\n");
        return NULL;
    }

    Vector *vector = (Vector *)malloc(sizeof(Vector));
    vector->size = matrix->cols;
    vector->data = (int *)malloc(matrix->cols * sizeof(int));
    for (int j = 0; j < matrix->cols; j++)
    {
        vector->data[j] = matrix->data[row_index][j];
    }
    return vector;
}

// Function to add a scalar to all elements of a vector
Vector *add_scalar_to_vector(Vector *vector, int scalar)
{
    Vector *result = (Vector *)malloc(sizeof(Vector));
    result->size = vector->size;
    result->data = (int *)malloc(vector->size * sizeof(int));
    for (int i = 0; i < vector->size; i++)
    {
        result->data[i] = vector->data[i] + scalar;
    }
    return result;
}

// Function to multiply a vector by a scalar and return the result as a new vector
Vector *multiply_vector_by_scalar(Vector *vector, int scalar)
{
    Vector *result = (Vector *)malloc(sizeof(Vector));
    result->size = vector->size;
    result->data = (int *)malloc(vector->size * sizeof(int));
    for (int i = 0; i < vector->size; i++)
    {
        result->data[i] = vector->data[i] * scalar;
    }
    return result;
}

// Function to free allocated memory for a matrix
void free_matrix(Matrix *matrix)
{
    for (int i = 0; i < matrix->rows; i++)
    {
        free(matrix->data[i]);
    }
    free(matrix->data);
    free(matrix);
}

// Function to free allocated memory for a vector
void free_vector(Vector *vector)
{
    free(vector->data);
    free(vector);
}

// Utility function to count rows and columns in a CSV file
void count_rows_and_cols(const char *filename, int *rows, int *cols)
{
    FILE *file = fopen(filename, "r");
    if (!file)
        return;

    *rows = 0;
    *cols = 0;
    char ch;
    int temp_cols = 0;

    while ((ch = fgetc(file)) != EOF)
    {
        if (ch == ',')
        {
            temp_cols++;
        }
        else if (ch == '\n')
        {
            (*rows)++;
            if (*cols == 0)
            {
                *cols = temp_cols + 1;
            }
            temp_cols = 0;
        }
    }
    if (temp_cols > 0)
    {
        (*rows)++;
        if (*cols == 0)
        {
            *cols = temp_cols + 1;
        }
    }

    fclose(file);
}