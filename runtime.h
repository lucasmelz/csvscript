// runtime.h
#ifndef RUNTIME_H
#define RUNTIME_H

// Matrix struct definition
typedef struct
{
    int **data;
    int rows;
    int cols;
} Matrix;

// Vector struct definition
typedef struct
{
    int *data;
    int size;
} Vector;

// Function prototypes
Matrix *load_matrix_from_csv(const char *filename);
void save_matrix_to_csv(const char *filename, Matrix *matrix);
Matrix *transpose_matrix(Matrix *matrix);
Matrix *multiply_matrices(Matrix *matrix1, Matrix *matrix2);
Matrix *multiply_matrix_by_scalar(Matrix *matrix, int scalar);
Matrix *add_scalar_to_matrix(Matrix *matrix, int scalar);
Vector *extract_row(Matrix *matrix, int row_index);
Vector *add_scalar_to_vector(Vector *vector, int scalar);
Vector *multiply_vector_by_scalar(Vector *vector, int scalar);
void free_matrix(Matrix *matrix);
void free_vector(Vector *vector);
void count_rows_and_cols(const char *filename, int *rows, int *cols);

#endif // RUNTIME_H