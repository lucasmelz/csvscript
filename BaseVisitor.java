import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import java.util.*;

public class BaseVisitor extends CsvScriptBaseVisitor<String> {
    private Set<String> persistentMatrices = new HashSet<>();
    private Set<String> temporaryMatrices = new HashSet<>();
    private Set<String> vectors = new HashSet<>();
    private Set<String> scalars = new HashSet<>();

    private static String CSV_SAMPLES_PATH = "./csv_samples/";

    @Override
    public String visitProg(CsvScriptParser.ProgContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include \"runtime.h\"\n");

        sb.append("int main() {\n");

        for (CsvScriptParser.StatContext statCtx : ctx.stat()) {
            sb.append(visit(statCtx));
        }

        // Save persistent matrices to CSV files before exiting
        for (String varName : persistentMatrices) {
            sb.append(String.format("    save_matrix_to_csv(\"" + CSV_SAMPLES_PATH + "%s.csv\", %s);\n", varName,
                    varName));
            sb.append(String.format("    free_matrix(%s);\n", varName)); // Free the matrix memory
        }

        sb.append("    return 0;\n");
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public String visitPersistDeclStat(CsvScriptParser.PersistDeclStatContext ctx) {
        String varName = ctx.persistentDecl().ID().getText();
        persistentMatrices.add(varName);
        return String.format(
                "    // Load matrix from CSV file\n" +
                        "    Matrix *%s = load_matrix_from_csv(\"" + CSV_SAMPLES_PATH + "%s.csv\");\n" +
                        "    if (%s == NULL) {\n" +
                        "        fprintf(stderr, \"Failed to load matrix from CSV file.\\n\");\n" +
                        "        return 1;\n" +
                        "    }\n",
                varName, varName, varName);
    }

    @Override
    public String visitAssignStat(CsvScriptParser.AssignStatContext ctx) {
        String varName = ctx.assignment().ID().getText();
        String expr = visit(ctx.assignment().expr());

        if (persistentMatrices.contains(varName) || temporaryMatrices.contains(varName) || vectors.contains(varName)
                || scalars.contains(varName)) {
            return String.format("    %s = %s;\n", varName, expr);
        } else {

            // case 1 (selecting a row from a matrix):
            // check if the expression contains one of the temporary
            // matrices or persistent matrices followed by [index] and
            // then return the formatted instruction as an assignment
            // of a new matrix (with just one row)
            for (String matrix : persistentMatrices) {
                if (expr.matches(matrix + "\\[[0-9]+\\]")) {
                    vectors.add(varName);
                    return String.format("    Vector *%s = extract_row(%s, %s);\n", varName, matrix,
                            expr.substring(matrix.length() + 1, expr.length() - 1));
                }
            }
            for (String matrix : temporaryMatrices) {
                if (expr.matches(matrix + "\\[[0-9]+\\]")) {
                    vectors.add(varName);
                    return String.format("    Vector *%s = extract_row(%s, %s);\n", varName, matrix,
                            expr.substring(matrix.length() + 1, expr.length() - 1));
                }
            }

            // case 2 (selecting a scalar from a vector):
            // check if the expression contains one of the temporary
            // matrices or persistent matrices followed by [row-index][col-index] and
            // then return the formatted instruction as an assignment of an integer
            for (String vector : vectors) {
                if (expr.matches(vector + "\\[[0-9]+\\]")) {
                    String formattedExpr = expr.replaceAll(vector + "\\[([0-9]+)\\]", vector + "->data[$1]");
                    scalars.add(varName);
                    return String.format("    int %s = %s;\n", varName, formattedExpr);
                }
            }

            // case 3 (selecting a scalar from a matrix):
            // check if the expression contains one of the temporary
            // matrices or persistent matrices followed by [row-index][col-index] and
            // then return the formatted instruction as an assignment of an integer
            for (String matrix : persistentMatrices) {
                if (expr.matches(matrix + "\\[[0-9]+\\]\\[[0-9]+\\]")) {
                    String formattedExpr = expr.replaceAll(matrix + "\\[([0-9]+)\\]\\[([0-9]+)\\]",
                            matrix + "->data[$1][$2]");
                    scalars.add(varName);
                    return String.format("    int %s = %s;\n", varName, formattedExpr);
                }
            }
            for (String matrix : temporaryMatrices) {
                if (expr.matches(matrix + "\\[[0-9]+\\]\\[[0-9]+\\]")) {
                    String formattedExpr = expr.replaceAll(matrix + "\\[([0-9]+)\\]\\[([0-9]+)\\]",
                            matrix + "->data[$1][$2]");
                    scalars.add(varName);
                    return String.format("    int %s = %s;\n", varName, formattedExpr);
                }
            }

            // case 4 (temporary matrix assignment)
            // check if the expression contains a transpose operation,
            // or an operation involving one of the persistent or temporary matrices,
            // then add this matrix to the temporaryMatrices set and format the assignment
            // as a Matrix assignment
            if (expr.contains("transpose")) {
                temporaryMatrices.add(varName);
                return String.format("    Matrix *%s = %s;\n", varName, expr);
            }

            for (String matrix : persistentMatrices) {
                if (expr.contains(matrix)) {
                    temporaryMatrices.add(varName);
                    return String.format("    Matrix *%s = %s;\n", varName, expr);
                }
            }
            for (String matrix : temporaryMatrices) {
                if (expr.contains(matrix)) {
                    temporaryMatrices.add(varName);
                    return String.format("    Matrix *%s = %s;\n", varName, expr);
                }
            }

            // Default case: assume scalar if no other type matches
            scalars.add(varName);
            return String.format("    int %s = %s;\n", varName, expr);
        }
    }

    @Override
    public String visitCommonExpr(CsvScriptParser.CommonExprContext ctx) {
        return String.format("    %s;\n", visit(ctx.expr()));
    }

    @Override
    public String visitMulDiv(CsvScriptParser.MulDivContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));

        // T__2 is the '*' token
        if (ctx.op.getType() == CsvScriptParser.T__2) {

            // if both sides are matrices
            if ((persistentMatrices.contains(left) || temporaryMatrices.contains(left)) &&
                    (persistentMatrices.contains(right) || temporaryMatrices.contains(right))) {
                return String.format("multiply_matrices(%s, %s)", left, right);
            }

            // if left side is a vector and assuming right side is a scalar
            if (vectors.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("multiply_vector_by_scalar(%s, %s)", left, right);
            }

            // if left side is a matrix and assuming right side is a scalar
            if (persistentMatrices.contains(left)
                    || temporaryMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("multiply_matrix_by_scalar(%s, %s)", left, right);
            }

            // if right side is a vector and assuming left side is a scalar
            if (vectors.contains(right) && (scalars.contains(left) || isScalar(left))) {
                return String.format("multiply_vector_by_scalar(%s, %s)", right, left);
            }

            // if right side is a matrix and assuming left side is a scalar
            if (persistentMatrices.contains(right)
                    || temporaryMatrices.contains(right) && (scalars.contains(left) || isScalar(left))) {
                return String.format("multiply_matrix_by_scalar(%s, %s)", right, left);
            }

            // both sides are scalars
            if ((scalars.contains(left) || isScalar(left)) && (scalars.contains(right) || isScalar(right))) {
                return String.format("%s * %s", left, right);
            }

            throw new RuntimeException("Compilation error: unknown types involved in multiplication operation.");
        } else {
            // Division operation

            // Divide vector by a scalar
            if (vectors.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("divide_vector_by_scalar(%s, %s)", left, right);
            }

            // if both sides are matrices, the operation is invalid
            if ((persistentMatrices.contains(left) || temporaryMatrices.contains(left)) &&
                    (persistentMatrices.contains(right) || temporaryMatrices.contains(right))) {
                throw new RuntimeException("Matrix division is not supported");
            }

            // if left side is a matrix and right side is a scalar
            if (persistentMatrices.contains(left)
                    || temporaryMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("divide_matrix_by_scalar(%s, %s)", left, right);
            }

            // if right side is a matrix and left side is a scalar, operation is invalid
            if (persistentMatrices.contains(right)
                    || temporaryMatrices.contains(right) && (scalars.contains(left) || isScalar(left))) {
                throw new RuntimeException("Scalar division by matrix is not possible");
            }

            // if right side is a vector and left side is a scalar, operation is invalid
            if (vectors.contains(right) && (scalars.contains(left) || isScalar(left))) {
                throw new RuntimeException("Scalar division by vector is not possible");
            }

            // both sides are scalars
            if ((scalars.contains(left) || isScalar(left)) && (scalars.contains(right) || isScalar(right))) {
                return String.format("%s / %s", left, right);
            }

            throw new RuntimeException("Compilation error: unknown types involved in division operation.");
        }
    }

    @Override
    public String visitAddSub(CsvScriptParser.AddSubContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));

        // T__4 is the '+' token
        if (ctx.op.getType() == CsvScriptParser.T__4) {

            // if both sides are matrices
            if ((persistentMatrices.contains(left) || temporaryMatrices.contains(left)) &&
                    (persistentMatrices.contains(right) || temporaryMatrices.contains(right))) {
                return String.format("add_matrices(%s, %s)", left, right);
            }

            // if left side is a matrix and right side is a scalar
            if (persistentMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_matrix(%s, %s)", left, right);
            }
            if (temporaryMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_matrix(%s, %s)", left, right);
            }

            // if right side is a matrix and left side is a scalar
            if (persistentMatrices.contains(right) && (scalars.contains(left) || isScalar(left))) {
                return String.format("add_scalar_to_matrix(%s, %s)", right, left);
            }
            if (temporaryMatrices.contains(right) && (scalars.contains(left) || isScalar(left))) {
                return String.format("add_scalar_to_matrix(%s, %s)", right, left);
            }

            // if left side is a vector and right side is a scalar
            if (vectors.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_vector(%s, %s)", left, right);
            }

            // if right side is a vector and left side is a scalar
            if (vectors.contains(right) && (scalars.contains(left) || isScalar(left))) {
                return String.format("add_scalar_to_vector(%s, %s)", right, left);
            }

            // if both sides are scalars
            if ((scalars.contains(left) || isScalar(left)) && (scalars.contains(right) || isScalar(right))) {
                return String.format("%s + %s", left, right);
            }

            throw new RuntimeException("Compilation error: unknown types involved in addition operation.");
        } else {
            // Subtraction operation

            // If left side is a vector and assuming right side is a scalar
            if (vectors.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_vector(%s, -%s)", left, right);
            }

            // if left side is a matrix and right side is a scalar
            if (persistentMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_matrix(%s, -%s)", left, right);
            }
            if (temporaryMatrices.contains(left) && (scalars.contains(right) || isScalar(right))) {
                return String.format("add_scalar_to_matrix(%s, -%s)", left, right);
            }

            // if right side is a matrix and left side is a scalar, operation is invalid
            if ((temporaryMatrices.contains(right) || persistentMatrices.contains(right))
                    && (scalars.contains(left) || isScalar(left))) {
                throw new RuntimeException("Compilation error: you cannot divide a scalar by a matrix!");
            }

            // Both sides are scalars
            if ((scalars.contains(left) || isScalar(left)) && (scalars.contains(right) || isScalar(right))) {
                return String.format("%s - %s", left, right);
            }

            throw new RuntimeException("Compilation error: unknown types involved in subtraction operation.");
        }
    }

    @Override
    public String visitTranspose(CsvScriptParser.TransposeContext ctx) {
        String expr = visit(ctx.expr());
        return String.format("transpose_matrix(%s)", expr);
    }

    @Override
    public String visitArray(CsvScriptParser.ArrayContext ctx) {
        String id = ctx.ID().getText();
        String index = visit(ctx.expr());
        return String.format("%s[%s]", id, index);
    }

    @Override
    public String visitMatrix(CsvScriptParser.MatrixContext ctx) {
        String id = ctx.ID().getText();
        String rowIndex = visit(ctx.expr(0));
        String colIndex = visit(ctx.expr(1));
        return String.format("%s[%s][%s]", id, rowIndex, colIndex);
    }

    @Override
    public String visitNegativeNumber(CsvScriptParser.NegativeNumberContext ctx) {
        return String.format("-%s", visit(ctx.expr()));
    }

    @Override
    public String visitVar(CsvScriptParser.VarContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitInt(CsvScriptParser.IntContext ctx) {
        return ctx.INT().getText();
    }

    @Override
    public String visitParens(CsvScriptParser.ParensContext ctx) {
        return String.format("(%s)", visit(ctx.expr()));
    }

    @Override
    public String visitEmptyStat(CsvScriptParser.EmptyStatContext ctx) {
        return "";
    }

    public static boolean isScalar(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
