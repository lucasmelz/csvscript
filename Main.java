import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java -cp .:antlr.jar Main <path-to-csvs-file> <output-file-name>");
            return;
        }

        String csvsFilePath = args[0];
        String outputFileName = args[1];

        String csvsScriptContent = readCsvsFile(csvsFilePath);

        CsvScriptLexer lexer = new CsvScriptLexer(CharStreams.fromString(csvsScriptContent));
        CsvScriptParser parser = new CsvScriptParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.prog();

        BaseVisitor visitor = new BaseVisitor();
        String cCode = visitor.visit(tree);

        String cFilePath = outputFileName + ".c";
        writeCFile(cFilePath, cCode);

        compileAndRunCFile(cFilePath, outputFileName);
    }

    private static String readCsvsFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static void writeCFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }

    private static void compileAndRunCFile(String cFilePath, String outputBinaryPath)
            throws IOException, InterruptedException {
        String runtimePath = "./runtime.c"; // Specify the path to your runtime.c

        Process compileProcess = new ProcessBuilder("gcc", runtimePath, cFilePath, "-o", outputBinaryPath).inheritIO()
                .start();
        int compileExitCode = compileProcess.waitFor();

        if (compileExitCode != 0) {
            System.out.println("Compilation failed.");
            return;
        }

        File binaryFile = new File(outputBinaryPath);
        if (!binaryFile.exists()) {
            System.out.println("Compiled binary not found.");
            return;
        }

        Process runProcess = new ProcessBuilder("./" + outputBinaryPath).inheritIO().start();
        int runExitCode = runProcess.waitFor();

        if (runExitCode != 0) {
            System.out.println("Execution failed.");
        }
    }
}