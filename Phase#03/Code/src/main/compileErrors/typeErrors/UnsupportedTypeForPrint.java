package main.compileErrors.typeErrors;

import main.compileErrors.CompileError;

public class UnsupportedTypeForPrint extends CompileError {

    public UnsupportedTypeForPrint(int line) {
        super(line, "Unsupported type for print");
    }

}
