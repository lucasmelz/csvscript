import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Example 1
        String input1 = "persistent D\n" +
                "matrix2 = transpose D\n" +
                "result = D * matrix2\n" +
                "D = result + 10\n";

        // Example 2
        String input2 = "persistent A\n" +
                "persistent B\n" +
                "C = A + B\n" +
                "result = transpose C\n";

        // Example 3
        String input3 = "persistent A\n" +
                "persistent E\n" +
                "row = E[ 0 ]\n" +
                "row = row + 1\n" +
                "row = row - 1\n" +
                "row = row * 2\n" +
                "element = E[0][1]\n" +
                "D = transpose A\n" +
                "A = D * E\n";

        // List of test cases
        String[] inputs = { input1, input2, input3 };

        // Process each input
        for (int i = 0; i < inputs.length; i++) {
            System.out.println("Example " + (i + 1) + ":\n");
            String input = inputs[i];

            CsvScriptLexer lexer = new CsvScriptLexer(CharStreams.fromString(input));
            CsvScriptParser parser = new CsvScriptParser(new CommonTokenStream(lexer));
            ParseTree tree = parser.prog();

            BaseVisitor visitor = new BaseVisitor();
            String cCode = visitor.visit(tree);

            System.out.println(cCode);
            System.out.println("\n---------------------------------------\n");
        }
    }
}