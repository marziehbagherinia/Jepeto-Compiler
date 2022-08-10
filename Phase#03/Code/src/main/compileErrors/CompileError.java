package main.compileErrors;

public class CompileError extends Exception {
    private final int line;
    protected String message;

    public CompileError(int line, String message) {
        this.line = line;
        this.message = message;
    }

    public String getMessage() {
        return "Line " + line + ": " + message;
    }
}
