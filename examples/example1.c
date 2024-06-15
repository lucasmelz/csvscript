#include <stdio.h>
#include <stdlib.h>
#include "runtime.h"
int main() {
    // Load matrix from CSV file
    Matrix *A = load_matrix_from_csv("./csv_samples/A.csv");
    if (A == NULL) {
        fprintf(stderr, "Failed to load matrix from CSV file.\n");
        return 1;
    }
    // Load matrix from CSV file
    Matrix *B = load_matrix_from_csv("./csv_samples/B.csv");
    if (B == NULL) {
        fprintf(stderr, "Failed to load matrix from CSV file.\n");
        return 1;
    }
    Vector *row = extract_row(B, 1);
    row = add_scalar_to_vector(row, 10);
    int element = row->data[0];
    Matrix *C = multiply_matrix_by_scalar(A, element);
    B = transpose_matrix(C);
    save_matrix_to_csv("./csv_samples/A.csv", A);
    free_matrix(A);
    save_matrix_to_csv("./csv_samples/B.csv", B);
    free_matrix(B);
    return 0;
}
